package gr.modissense.core;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import gr.modissense.ui.view.MultiChoice;
import gr.modissense.ui.view.MultiChoiceItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModiUserInfo implements Serializable {
    @Expose
    String username;
    @Expose
    List<Network> connections;
    @Expose
    String image;
    @Expose
    String primarySn;

    public List<UserExpanded> getAllFriends() {
        List<UserExpanded> usersExpanded = new ArrayList<UserExpanded>();
        if (connections != null) {
            for (Network n : connections) {
                for (NetworkConnectionInfo i : n.friends) {
                    usersExpanded.add(new UserExpanded(i.id, i.name, n.network, i.url));
                }
            }
        }
        return usersExpanded;
    }

    public List<String> getAllFriendsString() {
        List<String> usersExpanded = new ArrayList<String>();
        if (connections != null) {
            for (Network n : connections) {
                for (NetworkConnectionInfo i : n.friends) {
                    usersExpanded.add(new UserExpanded(i.id, i.name, n.network, i.url).toString());
                }
            }
        }
        return usersExpanded;
    }

    @Override
    public String toString() {
        return "ModiUserInfo{" +
                "username='" + username + '\'' +
                ", connections=" + connections +
                '}';
    }

    public static class ModiUserWrapper implements Serializable {
        ModiUserInfo user;

        @Override
        public String toString() {
            return "ModiUserWrapper{" +
                    "user=" + user +
                    '}';
        }
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPrimarySn() {
        return primarySn;
    }

    public void setPrimarySn(String primarySn) {
        this.primarySn = primarySn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static class UserExpanded implements Serializable, MultiChoiceItem {
        String id;
        String name;
        String url;
        String network;

        public UserExpanded() {
        }

        public UserExpanded(String id, String name, String network, String url) {
            this.id = id;
            this.name = name;
            this.network = network;
            this.url = url;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNetwork() {
            return network;
        }

        public void setNetwork(String network) {
            this.network = network;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return name + ":" + network;
        }
    }

    public static class Network implements Serializable {
        String network;
        List<NetworkConnectionInfo> friends;

        @Override
        public String toString() {
            return "Network{" +
                    "network='" + network + '\'' +
                    ", friends=" + friends +
                    '}';
        }
    }

    public static class NetworkConnectionInfo implements Serializable {
        String id;
        String name;
        String url;

        @Override
        public String toString() {
            return "NetworkConnectionInfo{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }


    public static final String TESTSTRING = "{\"user\":{\"username\":\"kmangr\",\"connections\":[{\"network\":\"twitter\",\"friends\":[{\"id\":\"1597968769\",\"name\":\"gem_2014\"},{\"id\":\"14321367\",\"name\":\"nuwanda969\"},{\"id\":\"211939295\",\"name\":\"Agissilaos\"},{\"id\":\"1758271\",\"name\":\"Sokratis\"},{\"id\":\"613364911\",\"name\":\"alonistiotis\"},{\"id\":\"431538314\",\"name\":\"FreeThinkinZone\"},{\"id\":\"28364108\",\"name\":\"nkarousos\"},{\"id\":\"312523029\",\"name\":\"tlatsas\"},{\"id\":\"1333500012\",\"name\":\"themisandrikop\"},{\"id\":\"235144727\",\"name\":\"IHU_SciTech\"},{\"id\":\"20229946\",\"name\":\"cmarinos\"},{\"id\":\"141131324\",\"name\":\"saikos\"},{\"id\":\"1412171436\",\"name\":\"hayah29\"},{\"id\":\"1364707459\",\"name\":\"Geotechniki\"},{\"id\":\"431429889\",\"name\":\"energiakoi\"},{\"id\":\"14236704\",\"name\":\"nevang\"},{\"id\":\"375745240\",\"name\":\"PolytexnikaNea\"},{\"id\":\"267750784\",\"name\":\"dipetr\"},{\"id\":\"105532981\",\"name\":\"billinflames\"},{\"id\":\"275882593\",\"name\":\"lawgreece\"},{\"id\":\"381459404\",\"name\":\"StavrosVassos\"},{\"id\":\"100460444\",\"name\":\"Jtsigkos\"},{\"id\":\"306900274\",\"name\":\"FotisRigas\"},{\"id\":\"299501508\",\"name\":\"sirmatsis\"},{\"id\":\"85368530\",\"name\":\"DimitrisIlias\"},{\"id\":\"26115869\",\"name\":\"StathisG\"},{\"id\":\"867551424\",\"name\":\"emhpee\"},{\"id\":\"244946026\",\"name\":\"ckoutras\"},{\"id\":\"27474851\",\"name\":\"doradidit\"},{\"id\":\"68925820\",\"name\":\"alxdoudou\"},{\"id\":\"890669034\",\"name\":\"8sphereGR\"},{\"id\":\"171491025\",\"name\":\"nikos_maroulis\"},{\"id\":\"213591442\",\"name\":\"GEWGreece\"},{\"id\":\"14459814\",\"name\":\"vtripolitakis\"},{\"id\":\"833714586\",\"name\":\"jbikakis_\"},{\"id\":\"56692107\",\"name\":\"Orfanidns\"},{\"id\":\"137427248\",\"name\":\"aouts\"},{\"id\":\"501105256\",\"name\":\"Reducecostinpar\"},{\"id\":\"549198800\",\"name\":\"IgasGr\"},{\"id\":\"14097484\",\"name\":\"gkriniaris\"},{\"id\":\"246399843\",\"name\":\"GMPIKIS\"},{\"id\":\"732157033\",\"name\":\"_Naif_Alshehri\"},{\"id\":\"371330388\",\"name\":\"kanaliotis\"},{\"id\":\"155150316\",\"name\":\"Funkieravi\"},{\"id\":\"163811204\",\"name\":\"ip_gr\"},{\"id\":\"1491971\",\"name\":\"kostis\"},{\"id\":\"594940640\",\"name\":\"filoikentrou\"},{\"id\":\"64187135\",\"name\":\"msgMani\"},{\"id\":\"106777529\",\"name\":\"openitgr\"},{\"id\":\"310201062\",\"name\":\"EliasLivadaras\"},{\"id\":\"434975241\",\"name\":\"raptismarine\"},{\"id\":\"217151866\",\"name\":\"margariteskipoi\"},{\"id\":\"562660542\",\"name\":\"FollowMachineTk\"},{\"id\":\"545968462\",\"name\":\"NexusLabGr\"},{\"id\":\"549029161\",\"name\":\"VPapagelopoulou\"},{\"id\":\"408379483\",\"name\":\"YahooNewsGR\"},{\"id\":\"393569910\",\"name\":\"TheGreeksEnergy\"},{\"id\":\"541824126\",\"name\":\"WMarketer\"},{\"id\":\"14398163\",\"name\":\"psalidas\"},{\"id\":\"17242502\",\"name\":\"aNTwNHs\"},{\"id\":\"158489841\",\"name\":\"denysonique\"},{\"id\":\"217911553\",\"name\":\"KastoriaGreece\"},{\"id\":\"90125341\",\"name\":\"ErTanmay\"},{\"id\":\"474356565\",\"name\":\"ma_skowron\"},{\"id\":\"509383713\",\"name\":\"Sexymagazino\"},{\"id\":\"54951537\",\"name\":\"spyrosl\"},{\"id\":\"198591178\",\"name\":\"angeloslenis\"},{\"id\":\"275249483\",\"name\":\"ddrakoulis\"},{\"id\":\"480192889\",\"name\":\"GreekSeo\"},{\"id\":\"439410220\",\"name\":\"gigsmesh\"},{\"id\":\"175428002\",\"name\":\"achatzakis\"},{\"id\":\"472512518\",\"name\":\"awsuggr\"},{\"id\":\"424087082\",\"name\":\"Omilos_MF\"},{\"id\":\"14932385\",\"name\":\"gkotsis\"},{\"id\":\"354131767\",\"name\":\"GUNdotIO\"},{\"id\":\"439349731\",\"name\":\"Paragoume\"},{\"id\":\"288056934\",\"name\":\"ppapapetrou76\"},{\"id\":\"19187566\",\"name\":\"gred\"},{\"id\":\"430462205\",\"name\":\"mletynski\"},{\"id\":\"62635183\",\"name\":\"androidgreece\"},{\"id\":\"399409772\",\"name\":\"rate24_gr\"},{\"id\":\"363353227\",\"name\":\"olafreeblog\"},{\"id\":\"255221909\",\"name\":\"JavaForums\"},{\"id\":\"388391480\",\"name\":\"CytaHost\"},{\"id\":\"14856056\",\"name\":\"AnemosNaftilos\"},{\"id\":\"74885931\",\"name\":\"PythonOrgGr\"},{\"id\":\"5546982\",\"name\":\"karounos\"},{\"id\":\"400466126\",\"name\":\"IriniHmin\"},{\"id\":\"386210347\",\"name\":\"mpakoyan\"},{\"id\":\"21602842\",\"name\":\"constantnos\"},{\"id\":\"156778249\",\"name\":\"greekeat\"},{\"id\":\"383716704\",\"name\":\"siuil83\"},{\"id\":\"18612002\",\"name\":\"hakmem\"},{\"id\":\"393923838\",\"name\":\"iakwvosspanidis\"},{\"id\":\"14287686\",\"name\":\"Argos_t\"},{\"id\":\"106774322\",\"name\":\"georgekoutras\"},{\"id\":\"14092028\",\"name\":\"saperduper\"},{\"id\":\"18863894\",\"name\":\"Minervity\"},{\"id\":\"14825149\",\"name\":\"Manogr\"},{\"id\":\"21035910\",\"name\":\"dpavlos\"},{\"id\":\"320419757\",\"name\":\"HellenismToday\"},{\"id\":\"184988911\",\"name\":\"clevernews\"},{\"id\":\"243614343\",\"name\":\"pinapps\"},{\"id\":\"217854903\",\"name\":\"polivlakas\"},{\"id\":\"18446155\",\"name\":\"argiropoulos_st\"},{\"id\":\"283038609\",\"name\":\"arapidhs\"},{\"id\":\"173539512\",\"name\":\"notyouraver4ge\"},{\"id\":\"13943142\",\"name\":\"xpanta\"},{\"id\":\"105537866\",\"name\":\"adheaven\"},{\"id\":\"98218628\",\"name\":\"Gofoboso\"},{\"id\":\"360590744\",\"name\":\"velti_demoday\"},{\"id\":\"371360070\",\"name\":\"TechstoriesBeat\"},{\"id\":\"22701817\",\"name\":\"agiotis\"},{\"id\":\"303124376\",\"name\":\"MariaIoann\"},{\"id\":\"14855132\",\"name\":\"FriedrichB\"},{\"id\":\"208461147\",\"name\":\"thanos_gkara\"},{\"id\":\"30042538\",\"name\":\"gkounenis\"},{\"id\":\"146236767\",\"name\":\"jpapani\"},{\"id\":\"85285113\",\"name\":\"AfterTax\"},{\"id\":\"17927498\",\"name\":\"aspaonline\"},{\"id\":\"10993312\",\"name\":\"TechblogGR\"},{\"id\":\"15055924\",\"name\":\"deltaHacker\"},{\"id\":\"18073954\",\"name\":\"gfotos\"},{\"id\":\"333888869\",\"name\":\"TheDateTheTime\"},{\"id\":\"246173864\",\"name\":\"DigiConsultGR\"},{\"id\":\"326781576\",\"name\":\"KliKaMe\"},{\"id\":\"321295064\",\"name\":\"affbuzzgr\"},{\"id\":\"319518835\",\"name\":\"MygreeceUSA\"},{\"id\":\"225468239\",\"name\":\"web4democracy\"},{\"id\":\"315129018\",\"name\":\"RigopoulosDimit\"},{\"id\":\"6919162\",\"name\":\"dealsend\"},{\"id\":\"12368782\",\"name\":\"spyros\"},{\"id\":\"11207492\",\"name\":\"e_diva\"},{\"id\":\"15357608\",\"name\":\"jsclavos\"},{\"id\":\"19225756\",\"name\":\"ktogias\"},{\"id\":\"235248737\",\"name\":\"ebalaskas\"},{\"id\":\"308968164\",\"name\":\"DiktioT\"},{\"id\":\"14316850\",\"name\":\"bibakis\"},{\"id\":\"24847686\",\"name\":\"Geopap\"},{\"id\":\"291879311\",\"name\":\"ticketwest\"},{\"id\":\"91292413\",\"name\":\"geo_pap\"},{\"id\":\"280370521\",\"name\":\"GDB_Greece\"},{\"id\":\"281542605\",\"name\":\"agrofestival\"},{\"id\":\"56046595\",\"name\":\"buzzlair\"},{\"id\":\"25071924\",\"name\":\"xristofo\"},{\"id\":\"127951583\",\"name\":\"b_thebest\"},{\"id\":\"28300534\",\"name\":\"skaragiannis\"},{\"id\":\"6411422\",\"name\":\"sVathis\"},{\"id\":\"271692529\",\"name\":\"fossAegean\"},{\"id\":\"250632949\",\"name\":\"bugsense\"},{\"id\":\"40420765\",\"name\":\"gaziotis\"},{\"id\":\"18062319\",\"name\":\"fotis_als\"},{\"id\":\"126425000\",\"name\":\"kyrcha\"},{\"id\":\"252534225\",\"name\":\"startupmarketgr\"},{\"id\":\"206156503\",\"name\":\"GreekPromotion\"},{\"id\":\"107136186\",\"name\":\"htheoharis\"},{\"id\":\"214612786\",\"name\":\"SoniaChalkidou\"},{\"id\":\"145844734\",\"name\":\"gregoryfarmakis\"},{\"id\":\"15681649\",\"name\":\"rizitis\"},{\"id\":\"260114380\",\"name\":\"SiliconEpiphany\"},{\"id\":\"252576843\",\"name\":\"localegr\"},{\"id\":\"17718789\",\"name\":\"aivalis\"},{\"id\":\"14450000\",\"name\":\"billias\"},{\"id\":\"246207345\",\"name\":\"dimiourgw\"},{\"id\":\"46085908\",\"name\":\"papadimi\"},{\"id\":\"120449794\",\"name\":\"tsakoyan\"},{\"id\":\"213263671\",\"name\":\"YouthInstitute\"},{\"id\":\"225516720\",\"name\":\"patrinistas\"},{\"id\":\"14175950\",\"name\":\"jonromero\"},{\"id\":\"17865999\",\"name\":\"PanosJee\"},{\"id\":\"207483074\",\"name\":\"govgr\"},{\"id\":\"18105878\",\"name\":\"theolam\"},{\"id\":\"14932449\",\"name\":\"jhug\"},{\"id\":\"157422963\",\"name\":\"sofiatsali\"},{\"id\":\"102634547\",\"name\":\"doleross\"},{\"id\":\"205305712\",\"name\":\"monogrinia\"},{\"id\":\"17201158\",\"name\":\"geobak\"},{\"id\":\"27216205\",\"name\":\"cherouvim\"},{\"id\":\"157959691\",\"name\":\"skoukios\"},{\"id\":\"16867002\",\"name\":\"ventrix\"},{\"id\":\"234040568\",\"name\":\"mrhousemesitiki\"},{\"id\":\"7425062\",\"name\":\"arkoudos\"},{\"id\":\"18757774\",\"name\":\"mparask\"},{\"id\":\"5790132\",\"name\":\"sotomi\"},{\"id\":\"36418965\",\"name\":\"peekly\"},{\"id\":\"213408296\",\"name\":\"TrelokomioGR\"},{\"id\":\"205653167\",\"name\":\"achillopoulos\"},{\"id\":\"16849746\",\"name\":\"mperedim\"},{\"id\":\"15212843\",\"name\":\"ikonomou\"},{\"id\":\"16293416\",\"name\":\"gkoutep\"},{\"id\":\"14759277\",\"name\":\"ktroulos\"},{\"id\":\"28973190\",\"name\":\"FINDBUYSALE\"},{\"id\":\"20193899\",\"name\":\"ebs_gr\"},{\"id\":\"92274250\",\"name\":\"mynetfolders\"},{\"id\":\"16293584\",\"name\":\"fstama\"},{\"id\":\"179095829\",\"name\":\"sportking_gr\"},{\"id\":\"141771128\",\"name\":\"PagesOnlineGr\"},{\"id\":\"9683862\",\"name\":\"chstath\"},{\"id\":\"1737011\",\"name\":\"javapapo\"},{\"id\":\"166086871\",\"name\":\"inlaconia\"},{\"id\":\"53922809\",\"name\":\"mukeshjee\"},{\"id\":\"148673965\",\"name\":\"skopelosmolos\"},{\"id\":\"27237233\",\"name\":\"elenafoutsi\"},{\"id\":\"14362672\",\"name\":\"pastith\"},{\"id\":\"34953531\",\"name\":\"Hippotas\"},{\"id\":\"18444288\",\"name\":\"flitzan\"}]}]}}";


    public static void main(String[] args) {
        System.out.println(new Gson().fromJson(TESTSTRING, ModiUserWrapper.class));
    }
}
