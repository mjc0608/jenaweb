package rdf;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;

import javax.sql.rowset.Predicate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SameName {
	private static Map<String, List<Statement>> sameName = new HashMap<String, List<Statement>>();
	private static Map<String, String> paper = new HashMap<String, String>();
	private static Map<String, List<String>> publication = new HashMap<String, List<String>>();
    static Model model, authorModel, authorListModel;

    private static void getModels() {
        model = FileManager.get().loadModel("./data/data.xml");
        authorModel = ModelFactory.createDefaultModel();
        authorListModel = ModelFactory.createDefaultModel();

        StmtIterator authorIter = model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
                    public boolean selects(Statement s) {
                        Resource subject = s.getSubject();
                        return (subject != null && (subject.toString().contains("http://localhost/author/") ||
                        							subject.toString().contains("http://localhost/journals/automatica/") ||
                        							subject.toString().contains("authorlist")));
                    }
                }
        );
        
        while (authorIter.hasNext()) {
            Statement s = authorIter.nextStatement();
            authorModel.add(s);
        }
    }

    private static void findSameName(Model model) {
        StmtIterator iter = model.listStatements();
        int sum = 0, papers = 0, authors = 0;
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();
  
            if (predicate.toString().equals("http://xmlns.com/foaf/0.1/name")) {
            	sum++;
            	String name = (object instanceof Resource)  ? (object.toString()) : (" \"" + object.toString() + "\"");  
            	List<Statement> list = (sameName.containsKey(name)) ? sameName.get(name) : new ArrayList<Statement>();
            	list.add(stmt);
            	sameName.put(name, list);
            }
            if (predicate.toString().equals("http://purl.org/dc/elements/1.1/title")) {
            	papers++;
            	String name = (object instanceof Resource)  ? (object.toString()) : (" \"" + object.toString() + "\"");  
            	String code = subject.toString().substring("http://localhost/journals/automatica/".length(), subject.toString().length());
            	paper.put(code, name);
            }
            if (subject.toString().contains("authorlist") && (!predicate.toString().contains("type"))) {
            	authors++;
            	String name = (object instanceof Resource)  ? (object.toString()) : (" \"" + object.toString() + "\"");
            	String code = name.substring("http://localhost/author/".length(), name.length());
            	String paper = subject.toString().substring("http://localhost/journals/automatica/".length(), subject.toString().length() - "/authorlist".length());
            	List<String> list = (publication.containsKey(code)) ? publication.get(code) : new ArrayList<String>();
            	list.add(paper);
            	publication.put(code, list);
            }
        }
    }
    
    private static void checkMap() {
    	int sum = 0, identical = 0, duplicated = 0;
    	for (Map.Entry<String, List<Statement>> entry: sameName.entrySet()) {
    		if (entry.getValue().size() > 1) { 
    			sum++;
    			duplicated += entry.getValue().size();
    		}
    		else {
    			identical++;
    			continue;
    		}
    		System.out.println("Name:" + entry.getKey());
    		for (Statement stmt : entry.getValue())
    			System.out.println("    " + stmt.getSubject().toString());
    	}
    	System.out.println(sum + " duplicated names found( with  " + duplicated + " authors ). "+ identical + " authors have identical names.");
    }
    
    private static void getJSON() {
    	try {
    		FileWriter f = new FileWriter("sameName.json");
    		f.write("{\"name\":\"SameName\",\"children\":[");
    		int x = 0;
    		for (Map.Entry<String, List<Statement>> entry: sameName.entrySet())
    			if (entry.getValue().size() > 1) {
    				ArrayList<String> authors = new ArrayList<String>();
    	        	List<Statement> list = entry.getValue();
    	        	for (Statement stmt : list)
    	        		authors.add(stmt.getSubject().toString());
    	        	String[] authorArray = authors.toArray(new String[authors.size()]);
    	        	if (x++ > 0) f.write(",");
    	        	f.write("{\"name\":" + entry.getKey() + ",\"children\":[");
    	        	int y = 0;
    	        	for (int i = 0; i < authorArray.length; i++) {
    	        		String id = authorArray[i].substring("http://localhost/author/".length(), authorArray[i].length());
    	        		if (y++ > 0) f.write(",");
    	        		f.write("{\"name\":\"" + id + "\",\"children\":[");
    	        		List<String> publications = publication.get(id);
    	        		if (publications == null || publications.isEmpty())
    	        			System.out.println("NO PAPER");
    	        		else {
    	        			int idx = 0;
	    	        		for (String p : publications) {
	    	        			if (p.contains("\"") || p.contains(".")) continue;
	    	        			if (idx++ > 0) f.write(",");
	    	        			f.write("{\"name\":" + paper.get(p) + "}");
	    	        		}
    	        		}
    	        		f.write("]}");
    	        	}
    	        	f.write("]}");
    			}
    		f.write("]}");
    		f.close();
    	} catch (IOException e) {
    		
    	}
    }

    public static void main(String[] args) {
        getModels();
        findSameName(authorModel);
        checkMap();
        getJSON();
        for (Map.Entry<String, List<Statement>> entry : sameName.entrySet()) 
        	if (entry.getValue().size() > 1) {
	        	ArrayList<String> authors = new ArrayList<String>();
	        	List<Statement> list = entry.getValue();
	        	for (Statement stmt : list)
	        		authors.add(stmt.getSubject().toString());
	           
	        	Property sameNameProp = authorModel.createProperty("http://localhost/samename/", "samename");
	        	Property authorProp = authorModel.createProperty("http://localhost/author/", "author");
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
	                    
	                    Resource coauthor = authorModel.getResource("http://localhost/author/" + author1id);
	                    author0.addProperty(sameNameProp, coauthor.getURI());
	                }
	            }
        	}
        
        try {
            PrintStream rdfout = new PrintStream(new File("./data/sameName.xml"));
            authorModel.write(rdfout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
