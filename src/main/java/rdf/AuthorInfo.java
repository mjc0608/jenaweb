package rdf;

import javafx.util.Pair;
import org.apache.jena.base.Sys;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashSet;

public class AuthorInfo {

    private static final String default_name = "Jiang Zemin";
    public static String default_query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
            "\n" +
            "SELECT DISTINCT ?comment WHERE {\n" +
            "  ?x0 rdf:type foaf:Person.\n" +
            "  ?x0 rdfs:label \"$$$$\"@en.\n" +
            "  ?x0 rdfs:comment ?comment.\n" +
            "  FILTER (LANG(?comment)='en')\n" +
            "}";

    public static String getAuthorInfoFromDBPedia(String name) {
        String comment = null;

        String queryString = default_query.replace("$$$$", name);
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);

        ResultSet results = qexec.execSelect();

        if (results.hasNext()) {
            comment = results.next().getLiteral("comment").toString();
            return comment;
        }

        qexec.close() ;

        if (comment == null) {
            comment = "No comment yet";
        }

//        ResultSetFormatter.out(System.out, results, query);

        return comment;
    }

    public static String authorUri;
    public static HashSet<String> authorListUris = new HashSet<String>();
    public static HashSet<String> paperUris = new HashSet<String>();
    public static HashSet<String> coauthorUris = new HashSet<String>();
    public static Model getAutorRelated(final String name) {
        Model model = FileManager.get().loadModel("./data/data.xml");
        Model authorModel = ModelFactory.createDefaultModel();
        authorListUris.clear();
        paperUris.clear();
        coauthorUris.clear();

        Property authorNameProp = authorModel.createProperty("http://xmlns.com/foaf/0.1/", "name");

        StmtIterator authorIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement s) {
                        Resource subject = s.getSubject();
                        Property predicate = s.getPredicate();
                        RDFNode object = s.getObject();
                        return (subject != null && subject.toString().contains("author")
                                && predicate != null && predicate.toString().contains("name")
                                && object != null && object.toString().toLowerCase().contains(name.toLowerCase()));
                    }
                }
        );
        if (!authorIter.hasNext()) {
            System.out.println("no such author");
            return  authorModel;
        }
        Statement authorStmt = authorIter.nextStatement();
        authorUri = authorStmt.getSubject().toString();

        System.out.println("author");
        authorIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement s) {
                        Resource subject = s.getSubject();
                        return (subject != null && subject.toString().equals(authorUri));
                    }
                }
        );
        while (authorIter.hasNext()) {
            Statement stmt = authorIter.nextStatement();
            authorModel.add(stmt);
            System.out.println(stmt.toString());
        }

        StmtIterator authorListIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement s) {
                        Resource subject = s.getSubject();
                        Property predicate = s.getPredicate();
                        RDFNode object = s.getObject();
                        return (subject != null && subject.toString().matches("http://localhost/journals/automatica/\\w+/authorlist")
                                && predicate != null && predicate.toString().contains("_")
                                && object != null && object.toString().contains(authorUri));
                    }
                }
        );
        while (authorListIter.hasNext()) {
            Statement stmt = authorListIter.next();
            String authorListUri = stmt.getSubject().toString();
            authorListUris.add(authorListUri);
            paperUris.add(authorListUri.substring(0, authorListUri.length() - "/authorlist".length()));
        }

        System.out.println("authorList");
        authorListIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement s) {
                        Resource subject = s.getSubject();
                        return (subject != null && authorListUris.contains(subject.toString()));
                    }
                }
        );
        while (authorListIter.hasNext()) {
            Statement stmt = authorListIter.nextStatement();
            authorModel.add(stmt);
            System.out.println(stmt.toString());
            if (stmt.getPredicate().toString().contains("_")
                    && !stmt.getObject().toString().equals(authorUri)
                    && !coauthorUris.contains(stmt.getObject().toString())) {
                coauthorUris.add(stmt.getObject().toString());
            }
        }

        System.out.println("paperAndCoauthor");
        System.out.println(paperUris.toString());
        StmtIterator paperAndCoauthorIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement s) {
                        Resource subject = s.getSubject();
                        return (subject != null && (coauthorUris.contains(subject.toString()) || paperUris.contains(subject.toString())));
                    }
                }
        );
        while (paperAndCoauthorIter.hasNext()) {
            Statement stmt = paperAndCoauthorIter.next();
            authorModel.add(stmt);
            System.out.println(stmt.toString());
        }

        return authorModel;
    }

    public static Pair<String, String> getAuthorRelatedJson(String name) {
        Model model = getAutorRelated(name);
        RDFParser rdfParser = new RDFParser(model);
        return XMLtoJson.xml2json(rdfParser.getCoauthor());
    }

    public static void main(String[] args) {
        String name = "Andrey V. Savkin";
        Pair<String, String> p = getAuthorRelatedJson(name);
        System.out.println(p.getKey());
        System.out.println(p.getValue());
    }
}