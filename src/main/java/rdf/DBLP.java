package rdf;

import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;


public class DBLP {
    private static Model model = null, authorModel, authorListModel, paperModel;

    private static Map<String, String> uriToAuthor, uriToTitle;

    private static Property SWRC, BIBO;

    public static boolean isAuthor(Statement stmt) {
        return stmt.getSubject().getPropertyResourceValue(RDF.type).getURI().equals("http://xmlns.com/foaf/0.1/Person");
    }

    public static boolean isAuthorList(Statement stmt) {
        return stmt.getSubject().getPropertyResourceValue(RDF.type).getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq");
    }

    public static boolean isPaper(Statement stmt) {
        return stmt.getSubject().getPropertyResourceValue(RDF.type).getURI().equals("http://swrc.ontoware.org/ontology#Article");
    }

    private static void getModels() {
        if (model != null) return;

        model = FileManager.get().loadModel("./data/data.xml");
        authorModel = ModelFactory.createDefaultModel();
        authorListModel = ModelFactory.createDefaultModel();
        paperModel = ModelFactory.createDefaultModel();
        SWRC = model.createProperty("http://swrc.ontoware.org/ontology#", "journal");
        BIBO = model.createProperty("http://purl.org/ontology/bibo/", "authorList");

        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            if (isAuthor(stmt)) {
                authorModel.add(stmt);
            }
            if (isAuthorList(stmt)) {
                authorListModel.add(stmt);
            }
            if (isPaper(stmt)) {
                paperModel.add(stmt);
            }
        }
    }

    private static void printModel(Model model) {
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();

            System.out.print(subject.toString());
            System.out.print(" " + predicate.toString() + " ");
            if (object instanceof Resource) {
                System.out.print(object.toString());
            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }
            System.out.println("");
        }
    }

    public static void mapAuthorWithUri() {
        uriToAuthor = new HashMap<String, String>();
        ResIterator authorIter = authorModel.listResourcesWithProperty(RDFS.label);
        while (authorIter.hasNext()) {
            Resource resource = authorIter.nextResource();
            Statement stmt = resource.getProperty(RDFS.label);
            uriToAuthor.put(stmt.getSubject().toString(), stmt.getObject().toString());
        }
    }

    public static void mapTitleWithUri() {
        uriToTitle = new HashMap<String, String>();
        ResIterator paperIter = paperModel.listResourcesWithProperty(RDFS.label);
        while (paperIter.hasNext()) {
            Resource resource = paperIter.nextResource();
            Statement stmt = resource.getProperty(RDFS.label);
            uriToTitle.put(stmt.getSubject().toString(), stmt.getObject().toString());
        }
    }

    public static void getAuthorPaper() {
        StmtIterator authorListIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement stmt) {
                        return isAuthorList(stmt);
                    }
                }
        );

        StmtIterator paperIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement stmt) {
                        return isPaper(stmt);
                    }
                }
        );

        Property paperUriProp = authorModel.createProperty("http://localhost/paperUri/", "uri");
        Property paperTitleProp = authorModel.createProperty("http://localhost/paperTitle/", "title");

        while (paperIter.hasNext()) {
            Statement stmt = paperIter.nextStatement();
//            System.out.println(stmt.getPredicate().toString());
            List<String> authors = new ArrayList<String>();
            String authorListUri = stmt.getObject().toString();
            if (authorListUri.matches("http://localhost/journals/automatica/\\w+/authorlist")) {
                String paperUri = stmt.getSubject().toString();
                for (int i = 1; i < 10; i++) {
                    Statement ordinal = authorListModel.getResource(authorListUri).getProperty(RDF.li(i));
                    if (ordinal == null) {
                        break;
                    }
//                    System.out.println(ordinal.getObject());
                    Resource resource = authorModel.getResource(ordinal.getObject().toString());
                    resource.addProperty(paperUriProp, paperUri + "; Title: " + uriToTitle.get(paperUri) + "; Rank: " + i);
//                    Resource tmp = resource.getProperty(paperUriProp).getResource();
//                    tmp.addProperty(paperTitleProp, uriToTitle.get(paperUri);
                }
//                System.out.println("======================================");
            }
        }

        try {
            PrintStream rdfout = new PrintStream(new File("./data/author.xml"));
            authorModel.write(rdfout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void getCoauthor() {
        StmtIterator authorListIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement stmt) {
                        Property predicate = s.getPredicate();
                        return (predicate != null && predicate.toString().contains("_1"));
                    }
                }
        );
        while (authorListIter.hasNext()) {
            Statement stmt = authorListIter.nextStatement();
            String authorListUri = stmt.getSubject().toString();
            Resource authorList = authorListModel.getResource(authorListUri);

            // 拿到一个特定的authorlist中所有的作者存到authors里面
            StmtIterator authorListStmtIter = authorList.listProperties();
            ArrayList<String> authors = new ArrayList<String>();
            while (authorListStmtIter.hasNext()) {
                Statement tmp = authorListStmtIter.nextStatement();
                RDFNode object = tmp.getObject();
                if (!object.toString().contains("http://localhost/author/")) {
                    continue;
                }
                authors.add(tmp.getObject().toString());
            }
//            System.out.println(authors);

            // 后面用来
            Property coauthorProp = authorModel.createProperty("http://localhost/coauthor/", "coauthor");
            Property authorProp = authorModel.createProperty("http://localhost/author/", "author");
            Property timeProp = authorModel.createProperty("http://localhost/time/", "time");

            String[] authorArray = authors.toArray(new String[authors.size()]);
            for (int i = 0; i < authorArray.length; i++) {
                Resource author0 = authorModel.getResource(authorArray[i]);
                String author0id = authorArray[i].substring("http://localhost/author/".length(), authorArray[i].length());
                for (int j = 0; j < authorArray.length; j++) {
                    if (j == i) {
                        continue;
                    }
                    Resource author1 = authorModel.getResource(authorArray[j]);
                    String author1id = authorArray[j].substring("http://localhost/author/".length(), authorArray[j].length());

                    // mmp，这个xml对嵌套支持不好，比如一个作者A有一个coauthor，需要一个field记录coauthor的uri，
                    // 还需要另一个field记录两人合作了多少次，只有单独加一个resource记录两个数据，然后作者A加一条引用到这个新增的resource
                    Resource coauthor = authorModel.getResource("http://localhost/coauthor/" + author0id + "/" + author1id);
                    Statement coauthorStm = coauthor.getProperty(authorProp);
                    if (coauthorStm == null || coauthorStm.getObject().toString() == "") {
                        coauthor.addProperty(authorProp, "http://localhost/author/" + author1id);
                        coauthor.addProperty(timeProp, "1");
                        author0.addProperty(coauthorProp, coauthor.getURI());
                    }
                    else {
                        Integer time = Integer.parseInt(coauthor.getProperty(timeProp).getObject().toString());
                        time++;
                        coauthor.getProperty(timeProp).changeObject(time.toString());
                    }
                }
            }
        }
//        printModel(authorModel);

        try {
            PrintStream rdfout = new PrintStream(new File("./data/coauthor.xml"));
            authorModel.write(rdfout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static boolean articleHasAuthor(String id, Resource authorList) {
        // 拿到一个特定的authorlist中所有的作者存到authors里面
        StmtIterator authorListStmtIter = authorList.listProperties();
        while (authorListStmtIter.hasNext()) {
            Statement tmp = authorListStmtIter.nextStatement();
            RDFNode object = tmp.getObject();
            if (!object.toString().contains("http://localhost/author/")) {
                continue;
            }
            if (object.toString().contains("http://localhost/author/"+id)) {
                return true;
            }
        }
        return false;
    }

    public static void getCoauthorByAuthor(String id) {
        StmtIterator authorListIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement stmt) {
                        return isAuthorList(stmt);
                    }
                }
        );

        while (authorListIter.hasNext()) {
            Statement stmt = authorListIter.nextStatement();
            String authorListUri = stmt.getSubject().toString();
            Resource authorList = authorListModel.getResource(authorListUri);

            if (!articleHasAuthor(id, authorList)) {
                continue;
            }

            // 拿到一个特定的authorlist中所有的作者存到authors里面
            StmtIterator authorListStmtIter = authorList.listProperties();
            ArrayList<String> authors = new ArrayList<String>();
            while (authorListStmtIter.hasNext()) {
                Statement tmp = authorListStmtIter.nextStatement();
                RDFNode object = tmp.getObject();
                if (!object.toString().contains("http://localhost/author/")) {
                    continue;
                }
                authors.add(tmp.getObject().toString());
            }

            // 后面用来
            Property coauthorProp = authorModel.createProperty("http://localhost/coauthor/", "coauthor");
            Property authorProp = authorModel.createProperty("http://localhost/author/", "author");
            Property timeProp = authorModel.createProperty("http://localhost/time/", "time");

            String[] authorArray = authors.toArray(new String[authors.size()]);
            for (int i = 0; i < authorArray.length; i++) {
                Resource author0 = authorModel.getResource(authorArray[i]);
                String author0id = authorArray[i].substring("http://localhost/author/".length(), authorArray[i].length());
                for (int j = 0; j < authorArray.length; j++) {
                    if (j == i) {
                        continue;
                    }
                    Resource author1 = authorModel.getResource(authorArray[j]);
                    String author1id = authorArray[j].substring("http://localhost/author/".length(), authorArray[j].length());

                    // mmp，这个xml对嵌套支持不好，比如一个作者A有一个coauthor，需要一个field记录coauthor的uri，
                    // 还需要另一个field记录两人合作了多少次，只有单独加一个resource记录两个数据，然后作者A加一条引用到这个新增的resource
                    Resource coauthor = authorModel.getResource("http://localhost/coauthor/" + author0id + "/" + author1id);
                    Statement coauthorStm = coauthor.getProperty(authorProp);
                    if (coauthorStm == null || coauthorStm.getObject().toString() == "") {
                        coauthor.addProperty(authorProp, "http://localhost/author/" + author1id);
                        coauthor.addProperty(timeProp, "1");
                        author0.addProperty(coauthorProp, coauthor.getURI());
                    } else {
                        Integer time = Integer.parseInt(coauthor.getProperty(timeProp).getObject().toString());
                        time++;
                        coauthor.getProperty(timeProp).changeObject(time.toString());
                    }
                }
            }
        }

        try {
            PrintStream rdfout = new PrintStream(new File("./data/coauthor-tmp.xml"));
            authorModel.write(rdfout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        getModels();

//        mapAuthorWithUri();
        mapTitleWithUri();

//        getCoauthor();

        getAuthorPaper();

    }
}