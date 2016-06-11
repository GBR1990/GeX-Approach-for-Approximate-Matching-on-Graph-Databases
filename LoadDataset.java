
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

/**
 * Loading the dataset. The data stored in BIOGRID-ORGANISM.csv is loaded in
 * Neo4J in the form of nodes and relationships. The Neo4J API is used to
 * achieve this. Each row of the dataset results in an edge of the data graph.
 * Only unique nodes are created and there are no duplicates in the data graph.
 * 
 * @author Gireeshma Reddy
 * @version 1.0
 * @since 2016-05-13
 */
public class LoadDataset {
	private static final File Neo4J_DBPath = new File("/Users/Lenovo/Downloads/Project/Db");
	static Node firstNode;
	static Node secondNode;
	static Relationship relation;
	GraphDatabaseService graphDataService;
	static Object InteractorA = null;
	static Object InteractorB = null;

	/*
	 * This method is used to form the relationship which is inserted into Neo4J
	 */
	private static enum RelTypes implements RelationshipType {
		INTERACTS
	}

	/*
	 * The Node labels are set as Interactor
	 */
	public enum MyLabels implements Label {
		Interactor
	}

	/*
	 * The main method inserts the values in the dataset into Neo4J using the
	 * Neo4J API.
	 */
	public static void main(String[] args) {

		Label myLabel = DynamicLabel.label("Interactor");

		try {
			BatchInserter inserter = BatchInserters.inserter(Neo4J_DBPath);
			/*
			 * The hashmap nodesInDatabase is used to keep a track of the nodes
			 * that have been inserted into Neo4J.
			 */
			Map<String, Object> nodesInDatabase = new HashMap<>();
			/*
			 * The hashmap mapUsedToInsertIntoDB is used as a temporary hashmap
			 * for batch insertion into Neo4J.
			 */
			Map<String, Object> mapUsedToInsertIntoDB = new HashMap<>();
			/*
			 * The hashmap hashmapOfNodeIDs has a list of each node and the node
			 * id that is assigned to it at the time of creation.
			 */
			Map<Object, String> hashmapOfNodeIDs = new HashMap<>();

			String csvFile = "BIOGRID-ORGANISM.csv";
			BufferedReader br = null;
			String line = "";
			String csvSplitBy = ",";
			int nodeNumber = 0;
			int csvRowNumber = 0;
			br = new BufferedReader(new FileReader(csvFile));

			while ((line = br.readLine()) != null) {
				csvRowNumber++;
				// As the first row of the csv contains the column names, it is
				// skipped
				if (csvRowNumber == 1) {
					continue;
				}
				String[] formattedDataset = line.split(csvSplitBy);

				InteractorA = formattedDataset[0];
				InteractorB = formattedDataset[1];
				//System.out.println("Edge [InteractorA= " + InteractorA + " , InteractorB=" + InteractorB + "]");

				long firstNodeId = 0L;
				long secondNodeId = 0L;
				// It is checked if the InteractorA of the present edge is
				// already present in the data graph
				if (!nodesInDatabase.containsValue((InteractorA))) {
					nodesInDatabase.put(String.valueOf(nodeNumber), InteractorA);
					nodeNumber++;
					mapUsedToInsertIntoDB.put("Name", InteractorA);

					firstNodeId = inserter.createNode(mapUsedToInsertIntoDB, MyLabels.Interactor);
					
					String first_NodeId = String.valueOf(firstNodeId);
					hashmapOfNodeIDs.put(InteractorA, first_NodeId);
					// It is checked if the InteractorB of the present edge is
					// already present in the data graph
					if (!nodesInDatabase.containsValue((InteractorB))) {
						nodesInDatabase.put(String.valueOf(nodeNumber), InteractorB);
						nodeNumber++;
						mapUsedToInsertIntoDB.put("Name", InteractorB);
						secondNodeId = inserter.createNode(mapUsedToInsertIntoDB, MyLabels.Interactor);
						
						String second_NodeId = String.valueOf(secondNodeId);
						hashmapOfNodeIDs.put(InteractorB, second_NodeId);

					} else // If the InteractorB is already present, it is used
							// to form the edge with the new InteractorA.
					{
						secondNodeId = Long.valueOf(hashmapOfNodeIDs.get(InteractorB));
						
					}

				} else { // If the InteractorA is already present, it is used to
							// form the edge.
					firstNodeId = Long.valueOf(hashmapOfNodeIDs.get(InteractorA));
					
					// It is checked if the InteractorB of the present edge is
					// already present in the data graph
					if (!nodesInDatabase.containsValue((InteractorB))) {
						nodesInDatabase.put(String.valueOf(nodeNumber), InteractorB);
						nodeNumber++;
						mapUsedToInsertIntoDB.put("Name", InteractorB);
						secondNodeId = inserter.createNode(mapUsedToInsertIntoDB, MyLabels.Interactor);

						
						String ss = String.valueOf(secondNodeId);
						hashmapOfNodeIDs.put(InteractorB, ss);

					} else/*
							 * If the InteractorB is already present, it is used
							 * to form the edge with the already present
							 * InteractorA.
							 */
					{
						secondNodeId = Long.valueOf(hashmapOfNodeIDs.get(InteractorB));
						
					}

				}
				// The relationship is created between the two nodes
				inserter.createRelationship(firstNodeId, secondNodeId, RelTypes.INTERACTS, null);

			}
			br.close();
			inserter.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
