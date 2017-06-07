package rdf;

import javafx.util.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by jachinma on 6/7/17.
 */
public class CoauthorInfo {

    private static Model model = null, authorListModel = null, paperModel = null, authorModel = null;


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
        authorListModel = ModelFactory.createDefaultModel();
        authorModel = ModelFactory.createDefaultModel();
        paperModel = ModelFactory.createDefaultModel();

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

    private static String getAuthorNameById(String id) {
        StmtIterator tmpIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement stmt) {
                        return stmt.getSubject().toString().contains(id) && stmt.getPredicate().toString().contains("name");
                    }
                }
        );
        return tmpIter.nextStatement().getObject().toString();
    }

    private static String getAuthorIdByName(String name) {
        if (model==null) getModels();

        StmtIterator tmpIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement stmt) {
                        return stmt.getObject().toString().toLowerCase().contains(name.toLowerCase()) && stmt.getPredicate().toString().contains("name");
                    }
                }
        );
        String id = tmpIter.nextStatement().getSubject().getURI().replace("http://localhost/author/", "");
        System.out.println(id);
        return id;
    }

    public static Model getCoauthorByAuthor(String id) {
        getModels();
        Model targetAuthorModel = ModelFactory.createDefaultModel();

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
            Property coauthorProp = targetAuthorModel.createProperty("http://localhost/coauthor/", "coauthor");
            Property authorProp = targetAuthorModel.createProperty("http://localhost/author/", "author");
            Property timeProp = targetAuthorModel.createProperty("http://localhost/time/", "time");
            Property name = targetAuthorModel.createProperty("http://localhost/name/", "name");
            Property label = targetAuthorModel.createProperty("http://localhost/label/", "label");


            String[] authorArray = authors.toArray(new String[authors.size()]);
            for (int i = 0; i < authorArray.length; i++) {
                Resource author0 = targetAuthorModel.getResource(authorArray[i]);
                String author0id = authorArray[i].substring("http://localhost/author/".length(), authorArray[i].length());
                for (int j = 0; j < authorArray.length; j++) {
                    if (j == i) {
                        continue;
                    }
                    Resource author1 = targetAuthorModel.getResource(authorArray[j]);
                    String author1id = authorArray[j].substring("http://localhost/author/".length(), authorArray[j].length());

                    // mmp，这个xml对嵌套支持不好，比如一个作者A有一个coauthor，需要一个field记录coauthor的uri，
                    // 还需要另一个field记录两人合作了多少次，只有单独加一个resource记录两个数据，然后作者A加一条引用到这个新增的resource
                    Resource coauthor = targetAuthorModel.getResource("http://localhost/coauthor/" + author0id + "/" + author1id);
                    Statement coauthorStm = coauthor.getProperty(authorProp);
                    if (coauthorStm == null || coauthorStm.getObject().toString() == "") {
                        coauthor.addProperty(authorProp, "http://localhost/author/" + author1id);
                        coauthor.addProperty(timeProp, "1");
                        author0.addProperty(coauthorProp, coauthor.getURI());
                        if (author0.getProperty(name)==null) {
                            author0.addProperty(name, getAuthorNameById(author0id));
                            author0.addProperty(label, getAuthorNameById(author0id));
                        }
                    } else {
                        Integer time = Integer.parseInt(coauthor.getProperty(timeProp).getObject().toString());
                        time++;
                        coauthor.getProperty(timeProp).changeObject(time.toString());
                    }
                }
            }
        }

        try {
            PrintStream rdfout = new PrintStream(new File("./data/coauthor-fuck.xml"));
            targetAuthorModel.write(rdfout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return targetAuthorModel;

    }

    private static final int minCoauthorNum = 1;

    private static Pair<String, String> getCoauthorJsonById(String authorID, Model m) {
        HashMap<String, Integer> authoridToIndex = new HashMap<String, Integer>();
        Integer index = 0;
        JSONArray authorsJson = JSONArray.fromObject("[]");
        JSONArray coauthorsJson = JSONArray.fromObject("[]");

        StmtIterator authorIter = m.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement s) {
                        Resource subject = s.getSubject();
                        Property predicate = s.getPredicate();
                        return (subject != null && subject.toString().contains("http://localhost/author/")
                                && predicate != null && predicate.toString().contains("name"));
                    }
                }
        );
        Property coauthorProp = m.getProperty("http://localhost/coauthor/", "coauthor");
        Property timeProp = m.createProperty("http://localhost/time/", "time");
        while (authorIter.hasNext()) {
            Statement stmt = authorIter.nextStatement();
            if (!authoridToIndex.containsKey(stmt.getSubject().toString())) {
                // 判断这个人是不是有coauthor，没有就跳过
                Boolean show = false;
                StmtIterator coauthorIter = m.listStatements(stmt.getSubject(), coauthorProp, (RDFNode)null);
                while (coauthorIter.hasNext()) {
                    Statement coStmt = coauthorIter.next();
                    Resource coRS = m.getResource(coStmt.getObject().toString());
                    if (Integer.parseInt(coRS.getProperty(timeProp).getObject().toString()) >= minCoauthorNum) {
                        show = true;
                        break;
                    }
                }
                if (!show) {
                    continue;
                }

                authoridToIndex.put(stmt.getSubject().toString()
                        .substring("http://localhost/author/".length(), stmt.getSubject().toString().length()), index++);
                JSONObject authorInfo = JSONObject.fromObject("{}");
                authorInfo.accumulate("name", stmt.getObject().toString());
                authorsJson.add(authorInfo);
            }
        }

        StmtIterator coauthorIter = m.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement s) {
                        Resource subject = s.getSubject();
                        Property predicate = s.getPredicate();
                        if (subject == null || !subject.toString().contains("http://localhost/coauthor/")
                                || predicate == null || !predicate.toString().contains("time")) {
                            return false;
                        }
                        Integer author0 = Integer.parseInt(subject.toString().substring(
                                "http://localhost/coauthor/".length(), "http://localhost/coauthor/000000".length()
                        ));
                        Integer author1 = Integer.parseInt(subject.toString().substring(
                                "http://localhost/coauthor/000000/".length(), subject.toString().length()
                        ));

                        return author0 < author1;
                    }
                }
        );
        while (coauthorIter.hasNext()) {
            Statement stmt = coauthorIter.nextStatement();
            if (Integer.parseInt(stmt.getObject().toString()) < minCoauthorNum) {
                continue;
            }

            JSONObject coauthorInfo = JSONObject.fromObject("{}");
            coauthorInfo.accumulate("source", authoridToIndex.get(stmt.getSubject().toString().substring(
                    "http://localhost/coauthor/".length(), "http://localhost/coauthor/000000".length()
            )));
            coauthorInfo.accumulate("target", authoridToIndex.get(stmt.getSubject().toString().substring(
                    "http://localhost/coauthor/000000/".length(), stmt.getSubject().toString().length()
            )));
            coauthorInfo.accumulate("value", stmt.getObject().toString());
            coauthorsJson.add(coauthorInfo);
        }

        return new Pair<String, String>(authorsJson.toString(), coauthorsJson.toString());
    }

    public static Pair<String, String> getCoauthorJsonByName(String name) {
        String id = getAuthorIdByName(name);
        Model authorModel = getCoauthorByAuthor(id);
        return getCoauthorJsonById(id, authorModel);
    }

    public static void main(String[] args) {

        String name = "Laurent El Ghaoui";
        Pair<String, String> p = getCoauthorJsonByName(name);
        System.out.println(p.getKey());
        System.out.println(p.getValue());
    }

}
