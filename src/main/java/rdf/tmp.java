//import com.hp.hpl.jena.ontology.OntModel;
//import com.hp.hpl.jena.ontology.OntProperty;
//import com.hp.hpl.jena.rdf.model.*;
//import com.hp.hpl.jena.util.FileManager;
//import com.hp.hpl.jena.vocabulary.RDF;
//import com.hp.hpl.jena.vocabulary.VCARD;
//
//import javax.sql.rowset.Predicate;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.PrintStream;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//
//// 这鬼玩意儿我用中文注释了！！！
//public class DBLP {
//    static Model model, authorModel, authorListModel;
//
//    public static boolean isAuthor(Statement stmt) {
//        return stmt.getSubject().getPropertyResourceValue(RDF.type).getURI().equals("http://xmlns.com/foaf/0.1/Person");
//    }
//
//    public static boolean isAuthorList(Statement stmt) {
//        return stmt.getSubject().getPropertyResourceValue(RDF.type).getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq");
//    }
//
//    public static boolean isPaper(Statement stmt) {
//        return stmt.getSubject().getPropertyResourceValue(RDF.type).getURI().equals("http://swrc.ontoware.org/ontology#Article");
//    }
//
//    private static void getModels() {
//        model = FileManager.get().loadModel("./data/data.xml");
//        authorModel = ModelFactory.createDefaultModel();
//        authorListModel = ModelFactory.createDefaultModel();
//
//        StmtIterator iter = model.listStatements();
//        while (iter.hasNext()) {
//            Statement stmt = iter.nextStatement();
//            if (isAuthor(stmt)) {
//                authorModel.add(stmt);
//            }
//            if (isAuthorList(stmt)) {
//                authorListModel.add(stmt);
//            }
//        }
//    }
//
//    private static void printModel(Model model) {
//        StmtIterator iter = model.listStatements();
//        while (iter.hasNext()) {
//            Statement stmt = iter.nextStatement();
//            Resource subject = stmt.getSubject();
//            Property predicate = stmt.getPredicate();
//            RDFNode object = stmt.getObject();
//
//            System.out.print(subject.toString());
//            System.out.print(" " + predicate.toString() + " ");
//            if (object instanceof Resource) {
//                System.out.print(object.toString());
//            } else {
//                // object is a literal
//                System.out.print(" \"" + object.toString() + "\"");
//            }
//            System.out.println("");
//        }
//    }
//
//    public static void getCoauthor() {
//        StmtIterator authorListIter = model.listStatements(
//                new SimpleSelector(null, null, (RDFNode) null) {
//                    public boolean selects(Statement stmt) {
//                        return isAuthorList(stmt);
//                    }
//                }
//        );
//        while (authorListIter.hasNext()) {
//            Statement stmt = authorListIter.nextStatement();
//            String authorListUri = stmt.getSubject().toString();
//            Resource authorList = authorListModel.getResource(authorListUri);
//
//            // 拿到一个特定的authorlist中所有的作者存到authors里面
//            StmtIterator authorListStmtIter = authorList.listProperties();
//            ArrayList<String> authors = new ArrayList<String>();
//            while (authorListStmtIter.hasNext()) {
//                Statement tmp = authorListStmtIter.nextStatement();
//                RDFNode object = tmp.getObject();
//                if (!object.toString().contains("http://localhost/author/")) {
//                    continue;
//                }
//                authors.add(tmp.getObject().toString());
//            }
////            System.out.println(authors);
//
//            // 后面用来
//            Property coauthorProp = authorModel.createProperty("http://localhost/coauthor/", "coauthor");
//            Property authorProp = authorModel.createProperty("http://localhost/author/", "author");
//            Property timeProp = authorModel.createProperty("http://localhost/time/", "time");
//
//            String[] authorArray = authors.toArray(new String[authors.size()]);
//            for (int i = 0; i < authorArray.length; i++) {
//                Resource author0 = authorModel.getResource(authorArray[i]);
//                String author0id = authorArray[i].substring("http://localhost/author/".length(), authorArray[i].length());
//                for (int j = 0; j < authorArray.length; j++) {
//                    if (j == i) {
//                        continue;
//                    }
//                    Resource author1 = authorModel.getResource(authorArray[j]);
//                    String author1id = authorArray[j].substring("http://localhost/author/".length(), authorArray[j].length());
//
//                    // mmp，这个xml对嵌套支持不好，比如一个作者A有一个coauthor，需要一个field记录coauthor的uri，
//                    // 还需要另一个field记录两人合作了多少次，只有单独加一个resource记录两个数据，然后作者A加一条引用到这个新增的resource
//                    Resource coauthor = authorModel.getResource("http://localhost/coauthor/" + author0id + "/" + author1id);
//                    Statement coauthorStm = coauthor.getProperty(authorProp);
//                    if (coauthorStm == null || coauthorStm.getObject().toString() == "") {
//                        coauthor.addProperty(authorProp, "http://localhost/author/" + author1id);
//                        coauthor.addProperty(timeProp, "1");
//                        author0.addProperty(coauthorProp, coauthor.getURI());
//                    }
//                    else {
//                        Integer time = Integer.parseInt(coauthor.getProperty(timeProp).getObject().toString());
//                        time++;
//                        coauthor.getProperty(timeProp).changeObject(time.toString());
//                    }
//                }
//            }
//        }
////        printModel(authorModel);
//
//        try {
//            PrintStream rdfout = new PrintStream(new File("./data/coauthor.xml"));
//            authorModel.write(rdfout);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void main(String[] args) {
//        getModels();
//
//        getCoauthor();
//    }
//}