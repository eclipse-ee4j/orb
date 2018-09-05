/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/*
 * SampleData.java
 *
 * Created on July 28, 2006, 10:06 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package performance.eesample;

import java.io.Serializable ;
import java.util.List ;
import java.util.ArrayList ;

/**
 *
 * @author ken
 */
public class SampleData {
    public static class Data implements Serializable {
        int id ;
        String name ;
        String cnpj ;
        String obs ;
    } 

    public List<Data> parse( String[][] data ) {
        List<Data> result = new ArrayList<Data>( data.length ) ;
        for (String[] rec : data) {
            Data drec = new Data() ;
            drec.id = Integer.valueOf( rec[0] ) ;
            drec.name = rec[1] ;
            drec.cnpj = rec[2] ;
            drec.obs = rec[3] ;
            result.add( drec ) ;
        }
        return result ;
    }

    // Does not propertly account for headers!
    public int estimateSize( List<Data> arg ) {
        int result = 4 ;
        for (Data data : arg) {
            int size = 0 ;
            size += 4 ;
            size += 4 ;
            size += data.name.length() + 1 ;
            size += data.cnpj.length() + 1 ; 
            size += data.obs.length() + 1 ;
            result += size ;
        }
        return result ;
    }

    public static final String[][] bankData = {
        {"1","Banco do Brasil S.A.","",""},
        {"2","Banco Central do Brasil","",""},
        {"3","Banco da Amaz\u00C3\u00B4nia S.A.","",""},
        {"4","Banco do Nordeste do Brasil S.A.","  .   .   /    -  ",""},
        {"5","Banco Nacional da Habita\u00C3\u00C7\u00C3\u00A3o","  .   .   /    -  ",""},
        {"6","Banco Nacional de Cr\u00C3\u00A9dito Cooperativo S.A.","  .   .   /    -  ",""},
        {"8","Banco Meridional S.A.","  .   .   /    -  ",""},
        {"10","Montenegro Business Corporation","01.212.121/0001-13",""},
        {"20","Banco do Estado de Alagoas S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"21","Banestes S.A. Banco do Estado do Espirito Santo","",""},
        {"24","Banco do Estado de Pernambuco S.A.","",""},
        {"26","Banco do Estado do Acre S.A.","",""},
        {"27","Banco do Estado de Santa Catarina S.A.","",""},
        {"28","Banco Baneb S.A.","",""},
        {"29","Banco Banerj S.A.","",""},
        {"30","Paraiban - Banco do Estado da Para\u00C3\u00ADba S.A.","",""},
        {"31","Banco do Estado de Goi\u00C3\u00A1s S.A.","",""},
        {"32","Banco do Estado de Mato Grosso S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"33","Banco do Estado de S\u00C3\u00A3o Paulo S.A. - Banespa","",""},
        {"34","Banco do Estado do Amazonas S.A.","",""},
        {"35","Banco do Estado do Ceara S.A.","",""},
        {"36","Banco do Estado do Maranh\u00C3\u00A3o S.A.","",""},
        {"37","Banco do Estado do Par\u00C3\u00A1 S.A.","",""},
        {"38","Banco do Estado do Paran\u00C3\u00A1 S.A.","",""},
        {"39","Banco do Estado do Piau\u00C3\u00AD S.A.","",""},
        {"40","Banco Cargill S.A.","",""},
        {"41","Banco do Estado do Rio Grande do Sul S.A.","",""},
        {"42","Banco J. Safra S.A.","",""},
        {"43","Banco do Estado do Rio Grande do Norte S.A.","",""},
        {"47","Banco do Estado de Sergipe S.A.","",""},
        {"48","Banco Bemge S.A.","",""},
        {"50","Banco de Des. do Est. do Rio de Janeiro S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"52","Banco de Des. do Est. do Maranh\u00C3\u00A3o S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"53","Banco de Desenvolvimento do Ceara S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"54","Banco de Desenvolvimento do Est. de S\u00C3\u00A3o Paulo S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"55","Banco de Desenvolvimento do Rio Grande do Norte S.A.","",""},
        {"56","Banco de Desenvolvimento do Estado do Rio Grande do Sul S.A.","",""},
        {"59","Banco do Estado de Rond\u00C3\u00B4nia S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"70","BRB - Banco de Bras\u00C3\u00ADlia S.A.","",""},
        {"71","Banco do Estado do Rio de Janeiro S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"104","Caixa Econ\u00C3\u00B4mica Federal","",""},
        {"106","Banco Itabanco S.A.","",""},
        {"107","Banco BBM S.A.","",""},
        {"109","Banco Credibanco S.A.","",""},
        {"116","Banco BNL do Brasil S.A.","",""},
        {"148","Multi Banco S.A.","",""},
        {"150","Caixa Econ\u00C3\u00B4mica do Estado de Minas Gerais","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"151","Nossa Caixa - Nosso Banco S.A.","",""},
        {"152","Caixa Econ\u00C3\u00B4mica do Estado de Goi\u00C3\u00A1s","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"153","Caixa Econ\u00C3\u00B4mica Estadual do Rio Grande do Sul","",""},
        {"154","Caixa Econ\u00C3\u00B4mica do Estado de Santa Catarina S.A.","",""},
        {"164","Banco Credit Commercial de France S.A.","",""},
        {"165","Banco Norchem S.A.","",""},
        {"166","Banco Inter-Atl\u00C3\u00A2ntico S.A.","",""},
        {"168","Banco CCF Brasil S.A.","",""},
        {"171","BFI - Banco de Financiamento Internacional S.A.","","Em fal\u00C3\u00AAncia"},
        {"175","Continental Banco S.A.","",""},
        {"184","Banco BBA-Creditanstalt S.A.","",""},
        {"199","Banco Financial Portugu\u00C3\u00AAs","",""},
        {"200","Banco Ficrisa Axelrud S.A.","",""},
        {"201","Banco Axial S.A.","",""},
        {"203","Banco Sibisa S.A. com. inv. cred. cons. cred. imob.","","Em fal\u00C3\u00AAncia"},
        {"204","Banco Inter American Express S.A.","",""},
        {"205","Banco Sul Am\u00C3\u00A9rica S.A.","",""},
        {"206","Banco Martinelli S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"207","Banco Garavelo S.A.","","Em fal\u00C3\u00AAncia"},
        {"208","Banco Pactual S.A.","",""},
        {"209","Agrobanco - Banco Comercial S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"210","Dresdner Bank Lateinamerika Aktiengesellschaft","",""},
        {"211","Banco Sistema S.A.","",""},
        {"212","Banco Matone S.A.","",""},
        {"213","Banco Arbi S.A.","",""},
        {"214","Banco Dibens S.A.","",""},
        {"215","Banco Am\u00C3\u00A9rica do Sul S.A.","",""},
        {"216","Banco Regional Malcon S.A. - Comerc. e de Cred. ao Consumidor","",""},
        {"217","Banco John Deere S.A.","",""},
        {"218","BBS Banco Bonsucesso S.A.","",""},
        {"219","Banco Zogbi S.A.","",""},
        {"220","Banco Crefisul S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"221","Banco Fleming Graphus S.A.","",""},
        {"222","Banco AGF Braseg S.A.","",""},
        {"223","Banco Interunion S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"224","Banco Fibra S.A.","",""},
        {"225","Banco Brascan S.A.","",""},
        {"226","Banco Auxiliar S.A.","",""},
        {"227","Banco Rosa S.A.","","Em fal\u00C3\u00AAncia"},
        {"228","Banco Icatu S.A.","",""},
        {"229","Banco Cruzeiro do Sul S.A.","",""},
        {"230","Banco Bandeirantes S.A.","",""},
        {"231","Banco Boavista Interatl\u00C3\u00A2ntico S.A.","",""},
        {"232","Banco Interpart S.A.","",""},
        {"233","Banco GE Capital S.A.","",""},
        {"234","Banco Lavra S.A.","",""},
        {"235","Banco Liberal S.A.","",""},
        {"236","Banco Cambial S.A.","",""},
        {"237","Banco Bradesco S.A.","",""},
        {"240","Banco de Credito Real de Minas Gerais S.A.","",""},
        {"241","Banco Cl\u00C3\u00A1ssico S.A.","",""},
        {"242","Banco Euroinvest S.A. - Eurobanco","",""},
        {"243","Banco Multi Stock S.A.","",""},
        {"244","Banco Cidade S.A.","",""},
        {"245","Banco Empresarial S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"246","Banco ABC Brasil S.A.","",""},
        {"247","Banco Warburg Dillon Read S.A.","",""},
        {"249","Banco Investcred S.A.","",""},
        {"250","Banco Schahin S.A.","",""},
        {"251","Banco S\u00C3\u00A3o Jorge S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"252","Banco Fininvest S.A.","",""},
        {"254","Paran\u00C3\u00A1 Banco S.A.","",""},
        {"255","Milbanco S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"256","Banco Gulfinvest S.A.","",""},
        {"258","Banco Induscred S.A.","",""},
        {"261","Banco Varig S.A.","",""},
        {"262","Banco Boreal S.A.","",""},
        {"263","Banco Cacique S.A.","",""},
        {"265","Banco Fator S.A.","",""},
        {"266","Banco C\u00C3\u00A9dula S.A.","",""},
        {"267","Banco BBM - Comercial, Cred. Imob. e Cred. Fin. Inv. S.A.","",""},
        {"275","Banco ABN Amro S.A.","",""},
        {"277","Planibanc Distribuidora de T\u00C3\u00ADtulos Valores Mobili\u00C3\u00A1rios S.A.","",""},
        {"282","Banco Brasileiro Comercial S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"284","Banco das Na\u00C3\u00C7\u00C3\u00B5es S.A.","",""},
        {"291","Banco de Cr\u00C3\u00A9dito Nacional S.A.","",""},
        {"294","BCR Banco de Cr\u00C3\u00A9dito Real S.A.","",""},
        {"295","Banco Crediplan S.A.","",""},
        {"298","Banco Alfa S.A.","",""},
        {"300","Banco de La Nacion Argentina","",""},
        {"302","Banco do Progresso S.A.","","Em fal\u00C3\u00AAncia"},
        {"303","Banco HNF S.A.","",""},
        {"304","Banco Pontual S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"308","Banco Comercial Bancesa S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"314","Banco do Comercio e Industria de S\u00C3\u00A3o Paulo S.A.","",""},
        {"317","Banco do Com\u00C3\u00A9rcio S.A.","",""},
        {"318","Banco BMG S.A.","",""},
        {"320","Banco Industrial e Comercial S.A.","",""},
        {"334","Banco Econ\u00C3\u00B4mico S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"337","Banco Savena S.A.","",""},
        {"338","Banco F. Barretto S.A.","",""},
        {"341","Banco Ita\u00C3\u00BA S.A.","",""},
        {"344","Banco Mercantil S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"345","Banco Financial S.A.","",""},
        {"347","Banco Sudameris Brasil S.A.","",""},
        {"351","Banco Bozano, Simonsen S.A.","",""},
        {"353","Banco Santander Brasil S.A.","",""},
        {"356","Banco Real Abn Amho S.A.","",""},
        {"361","Banco Industrial de Pernambuco S.A.","",""},
        {"366","Banco Sogeral S.A.","",""},
        {"369","Banco Digibanco S.A.","",""},
        {"370","Banco Europeu para a Am\u00C3\u00A9rica Latina (BEAL), S.A.","",""},
        {"372","Banco Itamarati S.A.","",""},
        {"375","Banco Fen\u00C3\u00ADcia S.A.","",""},
        {"376","Banco Chase Manhattan S.A.","",""},
        {"388","Banco BMD S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"389","Banco Mercantil do Brasil S.A.","",""},
        {"392","Banco Mercantil - Finasa S.A. - S\u00C3\u00A3o Paulo","",""},
        {"394","Banco BMC S.A.","",""},
        {"399","HSBC Bank Brasil S.A.","","Banco m\u00C3\u00BAltiplo"},
        {"405","Banco Mineiro S.A.","",""},
        {"409","Unibanco - Uni\u00C3\u00A3o de Bancos Brasileiros S.A.","",""},
        {"412","Banco Capital S.A.","",""},
        {"415","Banco Nacional S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"417","Banco Sul Brasileiro S.A.","",""},
        {"420","Banco Banorte S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"422","Banco Safra S.A.","",""},
        {"424","Banco Santander Noroeste S.A.","",""},
        {"432","Banco Pinto de Magalh\u00C3\u00A3es S.A.","",""},
        {"434","Banfort - Banco Fortaleza S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"441","Banco Real de S\u00C3\u00A3o Paulo S.A.","",""},
        {"446","Banco Regional S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"453","Banco Rural S.A.","",""},
        {"456","Banco de Tokyo-Mitsubishi Brasil S.A.","",""},
        {"464","Banco Sumitomo Brasileiro S.A.","",""},
        {"466","Banco de Tokyo S.A.","",""},
        {"472","Lloyds TSB Bank PLC","",""},
        {"474","Banco de Cr\u00C3\u00A9dito Comercial S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"477","Citibank N.A.","",""},
        {"479","Bankboston Banco M\u00C3\u00BAltiplo S.A.","",""},
        {"480","Banco Wachovia S.A.","",""},
        {"483","Banco Agrimisa S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"485","Banco de Roraima S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o"},
        {"486","Banco de Desenvolvimento do Paran\u00C3\u00A1 S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"487","Deutsche Bank S.A. Banco Alem\u00C3\u00A3o","",""},
        {"488","Morgan Guaranty Trust Company of New York","",""},
        {"489","Banco Franc\u00C3\u00AAs Uruguay S.A.","",""},
        {"490","Banco Resid\u00C3\u00AAncia S.A.","",""},
        {"491","Banco Maisonnave S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"492","Ing Bank N.V.","",""},
        {"493","Banco Union, C.A.","",""},
        {"494","Banco de La Republica Oriental Del Uruguay","",""},
        {"495","Banco de La Provincia de Buenos Aires","",""},
        {"496","Argentaria, Caja Postal y Banco Hipotecario S.A.","",""},
        {"497","Banco Hispano Americano S.A.","",""},
        {"498","Centro Hispano Banco","",""},
        {"499","Banco Iochpe S.A.","",""},
        {"500","Banco Habitasul S.A.","",""},
        {"501","Banco Brasileiro-Iraquiano S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"502","Banco Santander de Neg\u00C3\u00B3cios S.A.","",""},
        {"503","Banco BRJ S.A.","",""},
        {"504","Banco Multiplic S.A.","",""},
        {"505","Banco Credit Suisse First Boston Garantia S.A.","",""},
        {"600","Banco Luso Brasileiro S.A.","",""},
        {"601","BFC Banco S.A.","",""},
        {"602","Banco Patente S.A.","",""},
        {"603","Banco H\u00C3\u00A9rcules S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"604","Banco Industrial do Brasil S.A.","",""},
        {"605","BPA Banco P\u00C3\u00A3o de A\u00C3\u00C7\u00C3\u00BAcar S.A.","",""},
        {"607","Banco Santos Neves S.A.","",""},
        {"608","Banco Open S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"609","Banco Adolpho Oliveira & Associados S.A.","","Em fal\u00C3\u00AAncia"},
        {"610","Banco VR S.A.","",""},
        {"611","Banco Paulista S.A.","",""},
        {"612","Banco Guanabara S.A.","",""},
        {"613","Banco Pecunia S.A.","",""},
        {"616","Banco Interpacifico S.A.","",""},
        {"618","Banco Tendencia S.A.","",""},
        {"621","Banco Aplicap S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"622","Banco Dracma S.A.","",""},
        {"623","Banco Panamericano S.A.","",""},
        {"624","Banco General Motors S.A.","",""},
        {"625","Banco Arauc\u00C3\u00A1ria S.A.","",""},
        {"626","Banco Ficsa S.A.","",""},
        {"627","Banco Destak S.A.","",""},
        {"629","Banco Bancorp S.A.","","Em fal\u00C3\u00AAncia"},
        {"630","Banco Intercap S.A.","",""},
        {"631","Banco Columbia S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"633","Banco Rendimento S.A.","",""},
        {"634","Banco Tri\u00C3\u00A2ngulo S.A.","",""},
        {"635","Banco do Estado do Amap\u00C3\u00A1 S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"637","Banco Sofisa S.A.","",""},
        {"638","Banco Prosper S.A.","",""},
        {"639","Big S.A. - Banco Irm\u00C3\u00A3os Guimar\u00C3\u00A3es","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"641","Banco Bilbao Vizcaya Brasil S.A.","",""},
        {"642","Brasbanco S.A. Banco Comercial","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"643","Banco Pine S.A.","",""},
        {"645","Banco do Estado de Roraima S.A.","",""},
        {"647","Banco Marka S.A.","",""},
        {"648","Banco Atlantis S.A.","",""},
        {"649","Banco Dimensao S.A.","",""},
        {"650","Banco Pebb S.A.","",""},
        {"652","Banco Frances e Brasileiro S.A.","",""},
        {"653","Banco Indusval S.A.","",""},
        {"654","Banco A.J. Renner S.A.","  .   .   /    -  ",""},
        {"655","Banco Votorantim S.A.","",""},
        {"656","Banco Matrix S.A.","",""},
        {"657","Banco Tecnicorp S.A.","",""},
        {"658","Banco Porto Real S.A.","",""},
        {"702","Banco Santos S.A.","",""},
        {"705","Banco Investcorp S.A.","",""},
        {"707","Banco Daycoval S.A.","",""},
        {"711","Banco Vetor S.A.","",""},
        {"713","Banco Cindam S.A.","",""},
        {"715","Banco Vega S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"719","Banco Banif Primus S.A.","",""},
        {"720","Banco Maxinvest S.A.","",""},
        {"721","Banco Credibel S.A.","",""},
        {"722","Banco Interior de S\u00C3\u00A3o Paulo S.A.","",""},
        {"724","Banco Porto Seguro S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"725","Banco Finansinos S.A.","",""},
        {"726","Banco Universal S.A.","",""},
        {"727","Banco Comercial de S\u00C3\u00A3o Paulo S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o ordin\u00C3\u00A1ria"},
        {"728","Banco Fital S.A.","",""},
        {"729","Banco Fonte Cindam S.A.","",""},
        {"730","Banco Comercial Paraguayo S.A.","",""},
        {"731","Banco GNPP S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"732","Banco Minas S.A.","",""},
        {"734","Banco Gerdau S.A.","",""},
        {"735","Banco Pottencial S.A.","",""},
        {"736","Banco United S.A.","",""},
        {"737","Banco Theca S.A.","",""},
        {"738","Banco Morada S.A.","",""},
        {"739","Banco BGN S.A.","",""},
        {"740","Banco Barclays e Galicia S.A.","",""},
        {"741","Banco Ribeir\u00C3\u00A3o Preto S.A.","",""},
        {"742","Banco Equatorial S.A.","",""},
        {"743","Banco Emblema S.A.","",""},
        {"744","Bankboston, N.A.","",""},
        {"745","Banco Citibank S.A.","",""},
        {"746","Banco Modal S.A.","",""},
        {"747","Banco Rabobank International Brasil S.A.","",""},
        {"748","Banco Cooperativo Sicredi S.A. - Bansicredi","",""},
        {"749","BR Banco Mercantil S.A.","",""},
        {"750","Banco Republic National Bank of New York (Brasil), S.A.","",""},
        {"751","Dresdner Bank Brasil S.A. Banco M\u00C3\u00BAltiplo","",""},
        {"752","Banco Banque Nationale de Paris Brasil S.A.","",""},
        {"753","Banco Comercial Uruguai S.A.","",""},
        {"754","Banco Bamerindus do Brasil S.A.","","Liquida\u00C3\u00C7\u00C3\u00A3o extrajudicial"},
        {"755","Banco Merrill Lynch S.A.","",""},
        {"756","Banco Cooperativo do Brasil S.A.","",""},
        {"757","Banco Keb do Brasil S.A.","",""},
        {"1002","Teste automatizado - 1186813426861361525","12.345.678/0001-12","Observa\u00C3\u00C7\u00C3\u00A3o de exemplo para teste automatizado."},
        {"1003","Teste automatizado - 8958728301296053374","12.345.678/0001-12","Observa\u00C3\u00C7\u00C3\u00A3o de exemplo para teste automatizado."},
        {"1066","Osaman Banking","78.174.299/0001-98","Teste"}
    } ;

    public static void main( String[] args ) {
        SampleData ee = new SampleData() ;
        List<Data> myData = ee.parse( bankData ) ;
        int size = ee.estimateSize( myData ) ;
        System.out.println( "Data set size = " + size ) ;
    }
}
