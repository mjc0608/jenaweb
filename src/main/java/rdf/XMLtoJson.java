package rdf;

import javafx.util.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.auth.AUTH;
import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import javax.sql.rowset.Predicate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;

public class XMLtoJson {
    final static Integer minCoauthorNum = 0;

    public static Pair<String, String> xml2json(Model model) {
        HashMap<String, Integer> authoridToIndex = new HashMap<String, Integer>();
        Integer index = 0;
        JSONArray authorsJson = JSONArray.fromObject("[]");
        JSONArray coauthorsJson = JSONArray.fromObject("[]");

        StmtIterator authorIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement s) {
                        Resource subject = s.getSubject();
                        Property predicate = s.getPredicate();
                        return (subject != null && subject.toString().contains("http://localhost/author/")
                                && predicate != null && predicate.toString().contains("name"));
                    }
                }
        );
        Property coauthorProp = model.getProperty("http://localhost/coauthor/", "coauthor");
        Property timeProp = model.createProperty("http://localhost/time/", "time");
        while (authorIter.hasNext()) {
            Statement stmt = authorIter.nextStatement();
//            System.out.println(stmt.toString());
            if (!authoridToIndex.containsKey(stmt.getSubject().toString())) {
                // 判断这个人是不是有coauthor，没有就跳过
                Boolean show = false;
                StmtIterator coauthorIter = model.listStatements(stmt.getSubject(), coauthorProp, (RDFNode)null);
                while (coauthorIter.hasNext()) {
                    Statement coStmt = coauthorIter.next();
                    Resource coRS = model.getResource(coStmt.getObject().toString());
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
//        System.out.println(authorsJson);


        StmtIterator coauthorIter = model.listStatements(
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
//            System.out.println(stmt.toString());
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
//        System.out.println(coauthorsJson);

//        try {
//            PrintStream jsonOut = new PrintStream(new File("./data/coauthor-tmp.json"));
//            jsonOut.print("var nodes = ");
//            jsonOut.print(authorsJson);
//            jsonOut.println(";");
//            jsonOut.println();
//            jsonOut.print("var edges = ");
//            jsonOut.print(coauthorsJson);
//            jsonOut.println(";");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        return new Pair<String, String>(authorsJson.toString(), coauthorsJson.toString());
    }


    public static void main(String[] args) {
        Model m = AuthorInfo.getAutorRelated("Andrey V. Savkin");
        Pair<String, String> p = xml2json(m);
        System.out.println(p.getKey());
        System.out.println(p.getValue());
    }
}
