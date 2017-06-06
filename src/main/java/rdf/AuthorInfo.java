package rdf;

import org.apache.jena.query.*;

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

    public static void main(String[] args) {
        System.out.println(getAuthorInfoFromDBPedia("Anton Hofer"));
    }
}