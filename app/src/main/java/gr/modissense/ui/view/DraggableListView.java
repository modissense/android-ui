package gr.modissense.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

public class DraggableListView extends ListView {

    private static final String LOG_TAG = "tasks365";

    private static final int END_OF_LIST_POSITION = -2;

    private DropListener mDropListener;
    private int draggingItemHoverPosition;
    private int dragStartPosition; // where was the dragged item originally
    private int mUpperBound; // scroll the view when dragging point is moving out of this bound
    private int mLowerBound; // scroll the view when dragging point is moving out of this bound
    private int touchSlop;
    private Dragging dragging;
    private GestureDetector longPressDetector;

    public DraggableListView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.listViewStyle);
    }

    public DraggableListView(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        longPressDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
            @Override
            public void onLongPress(final MotionEvent e) {
                int x = (int) e.getX();
                final int y = (int) e.getY();
                int itemnum = pointToPosition(x, y);
                if (itemnum == AdapterView.INVALID_POSITION) {
                    return;
                }

                if (dragging != null) {
                    dragging.stop();
                    dragging = null;
                }

                final View item = getChildAt(itemnum - getFirstVisiblePosition());
                item.setPressed(false);
                dragging = new Dragging(getContext());
                dragging.start(y, ((int) e.getRawY()) - y, item);
                draggingItemHoverPosition = itemnum;
                dragStartPosition = draggingItemHoverPosition;

                int height = getHeight();
                mUpperBound = Math.min(y - touchSlop, height / 3);
                mLowerBound = Math.max(y + touchSlop, height * 2 / 3);
            }
        });

        setOnItemLongClickListener(new OnItemLongClickListener() {
            @SuppressWarnings("unused")
            @Override
            public boolean onItemLongClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
                // Return true to let AbsListView reset touch mode
                // Without this handler, the pressed item will keep highlight.
                return true;
            }
        });
    }

    /* pointToPosition() doesn't consider invisible views, but we need to, so implement a slightly different version. */
    private int myPointToPosition(int x, int y) {
        if (y < 0) {
            return getFirstVisiblePosition();
        }
        Rect frame = new Rect();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.getHitRect(frame);
            if (frame.contains(x, y)) {
                return getFirstVisiblePosition() + i;
            }
        }
        if ((x >= frame.left) && (x < frame.right) && (y >= frame.bottom)) {
            return END_OF_LIST_POSITION;
        }
        return INVALID_POSITION;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (longPressDetector.onTouchEvent(ev)) {
            return true;
        }

        if ((dragging == null) || (mDropListener == null)) {
            // it is not dragging, or there is no drop listener
            return super.onTouchEvent(ev);
        }

        int action = ev.getAction();
        switch (ev.getAction()) {

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging.stop();
                dragging = null;

                if (mDropListener != null) {
                    if (draggingItemHoverPosition == END_OF_LIST_POSITION) {
                        mDropListener.drop(dragStartPosition, getCount() - 1);
                    } else if (draggingItemHoverPosition != INVALID_POSITION) {
                        mDropListener.drop(dragStartPosition, draggingItemHoverPosition);
                    }
                }
                resetViews();
                break;

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                dragging.drag(x, y);
                int position = dragging.calculateHoverPosition();
                if (position != INVALID_POSITION) {
                    if ((action == MotionEvent.ACTION_DOWN) || (position != draggingItemHoverPosition)) {
                        draggingItemHoverPosition = position;
                        doExpansion();
                    }
                    scrollList(y);
                }
                break;
        }
        return true;
    }

    private void doExpansion() {
        int expanItemViewIndex = draggingItemHoverPosition - getFirstVisiblePosition();
        if (draggingItemHoverPosition >= dragStartPosition) {
            expanItemViewIndex++;
        }

        Log.v(LOG_TAG, "Dragging item hovers over position " + draggingItemHoverPosition + ", expand item at index "
                + expanItemViewIndex);

        View draggingItemOriginalView = getChildAt(dragStartPosition - getFirstVisiblePosition());
        for (int i = 0;; i++) {
            View itemView = getChildAt(i);
            if (itemView == null) {
                break;
            }
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            int height = LayoutParams.WRAP_CONTENT;
            if (itemView.equals(draggingItemOriginalView)) {
                height = 1;
            } else if (i == expanItemViewIndex) {
                height = itemView.getHeight() + dragging.getDraggingItemHeight();
            }
            params.height = height;
            itemView.setLayoutParams(params);
        }
    }

    /**
     * Reset view to original height.
     */
    private void resetViews() {
        for (int i = 0;; i++) {
            View v = getChildAt(i);
            if (v == null) {
                layoutChildren(); // force children to be recreated where needed
                v = getChildAt(i);
                if (v == null) {
                    break;
                }
            }
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = LayoutParams.WRAP_CONTENT;
            v.setLayoutParams(params);
        }
    }

    private void resetScrollBounds(int y) {
        int height = getHeight();
        if (y >= height / 3) {
            mUpperBound = height / 3;
        }
        if (y <= height * 2 / 3) {
            mLowerBound = height * 2 / 3;
        }
    }

    private void scrollList(int y) {
        resetScrollBounds(y);

        int height = getHeight();
        int speed = 0;
        if (y > mLowerBound) {
            // scroll the list up a bit
            speed = y > (height + mLowerBound) / 2 ? 16 : 4;
        } else if (y < mUpperBound) {
            // scroll the list down a bit
            speed = y < mUpperBound / 2 ? -16 : -4;
        }
        if (speed != 0) {
            int ref = pointToPosition(0, height / 2);
            if (ref == AdapterView.INVALID_POSITION) {
                //we hit a divider or an invisible view, check somewhere else
                ref = pointToPosition(0, height / 2 + getDividerHeight() + 64);
            }
            View v = getChildAt(ref - getFirstVisiblePosition());
            if (v != null) {
                int pos = v.getTop();
                setSelectionFromTop(ref, pos - speed);
            }
        }
    }

    public void setDropListener(DropListener l) {
        mDropListener = l;
    }

    public interface DropListener {
        void drop(int from, int to);
    }

    class Dragging {

        private Context context;
        private WindowManager windowManager;
        private WindowManager.LayoutParams mWindowParams;
        private ImageView mDragView;
        private Bitmap mDragBitmap;
        private int coordOffset;
        private int mDragPoint; // at what offset inside the item did the user grab it
        private int draggingItemHeight;
        private int x;
        private int y;
        private int lastY;

        public Dragging(Context context) {
            this.context = context;
            windowManager = (WindowManager) context.getSystemService("window");
        }

        /**
         * @param y
         * @param offset - the difference in y axis between screen coordinates and coordinates in this view
         * @param view - which view is dragged
         */
        public void start(int y, int offset, View view) {
            this.y = y;
            lastY = y;
            this.coordOffset = offset;
            mDragPoint = y - view.getTop();

            draggingItemHeight = view.getHeight();

            mDragView = new ImageView(context);
            mDragView.setBackgroundResource(android.R.drawable.alert_dark_frame);

            // Create a copy of the drawing cache so that it does not get recycled
            // by the framework when the list tries to clean up memory
            view.setDrawingCacheEnabled(true);
            mDragBitmap = Bitmap.createBitmap(view.getDrawingCache());
            mDragView.setImageBitmap(mDragBitmap);

            mWindowParams = new WindowManager.LayoutParams();
            mWindowParams.gravity = Gravity.TOP;
            mWindowParams.x = 0;
            mWindowParams.y = y - mDragPoint + coordOffset;
            mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mWindowParams.format = PixelFormat.TRANSLUCENT;
            mWindowParams.windowAnimations = 0;

            windowManager.addView(mDragView, mWindowParams);
        }

        public void drag(int x, int y) {
            lastY = this.y;
            this.x = x;
            this.y = y;
            mWindowParams.y = y - mDragPoint + coordOffset;
            windowManager.updateViewLayout(mDragView, mWindowParams);
        }

        public void stop() {
            if (mDragView != null) {
                windowManager.removeView(mDragView);
                mDragView.setImageDrawable(null);
                mDragView = null;
            }
            if (mDragBitmap != null) {
                mDragBitmap.recycle();
                mDragBitmap = null;
            }
        }

        public int getDraggingItemHeight() {
            return draggingItemHeight;
        }

        public int calculateHoverPosition() {
            int adjustedY = (int) (y - mDragPoint + (Math.signum(y - lastY) + 2) * draggingItemHeight / 2);
            // Log.v(LOG_TAG, "calculateHoverPosition(): lastY=" + lastY + ", y=" + y + ", adjustedY=" + adjustedY);
            int pos = myPointToPosition(0, adjustedY);
            if (pos >= 0) {
                if (pos >= dragStartPosition) {
                    pos -= 1;
                }
            }
            return pos;
        }

    }
}
