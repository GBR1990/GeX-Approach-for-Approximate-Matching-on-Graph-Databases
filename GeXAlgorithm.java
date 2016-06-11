import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * Applying the Graph Explorer algorithm. The edges forming the query graph are
 * taken from the user through the console. The top-k approximate matches for
 * each of the edges is given as output.
 * 
 * @author Gireeshma Reddy
 * @version 1.0
 * @since 2016-05-13
 */

public class GeXAlgorithm {
	private static final File neo4JFileAtDBPath = new File("/Users/Lenovo/Downloads/Project/Db");
	static Node firstNode;
	static Node secondNode;
	static Relationship relation;
	static GraphDatabaseService db;
	static Object InteractorA = null;
	static Object InteractorB = null;
	static HashMap hashmapWithUserInput = new HashMap();
	static int kOfTheGeXAlgorithm = 0;
	static int numbOfInputs = 0;
	/**
	 * hashmapWithStartNodes contains the edge as the key and an arraylist
	 * containing all the start nodes approximately matching as the input.
	 */
	static HashMap<String, ArrayList<String>> hashmapWithStartNodes = new HashMap<String, ArrayList<String>>();
	/**
	 * hashmapWithEndNodes contains the edge as the key and an arraylist
	 * containing all the end nodes approximately matching as the input.
	 */
	static HashMap<String, ArrayList<String>> hashmapWithEndNodes = new HashMap<String, ArrayList<String>>();

	static HashMap<String, ArrayList<String>> hashmapUsedToGenerateOutput = new HashMap<String, ArrayList<String>>();

	private static enum RelTypes implements RelationshipType {
		INTERACTS
	}

	public enum MyLabels implements Label {
		Interactor
	}

	/**
	 * askInputFromUser asks the user to enter the value of K and the required
	 * edges and stores it.
	 */
	public static void askInputFromUser() {
		Scanner inputFromConsole = new Scanner(System.in);
		System.out.println("Enter the value of K:");
		kOfTheGeXAlgorithm = Integer.parseInt(inputFromConsole.nextLine());
		System.out.println("Value of K is:" + kOfTheGeXAlgorithm);
		int numbOfInputEdges = 1;
		boolean b = true;
		while (b) {
			Scanner reader = new Scanner(System.in); // Reading from System.in
			System.out.println("Enter relationship (Format: X InteractsWith Y)" + numbOfInputEdges
					+ " or Enter END to stop entering: ");
			String inputString = reader.nextLine();

			if ((inputString.equals("END")) || (inputString.equals("end"))) {
				b = false;
			} else {
				hashmapWithUserInput.put(numbOfInputEdges, inputString);
				numbOfInputs++;
			}
			numbOfInputEdges++;
		}

		System.out.println("The inputs given by user are:");
		Set set = hashmapWithUserInput.entrySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry) it.next();
			System.out.print(me.getKey() + ": ");
			System.out.println(me.getValue());
		}

	}

	/**
	 * generateSubgraph finds the approximate path matching the query graph
	 */
	public static void generateSubgraph() {
		Set set3 = hashmapWithUserInput.entrySet();
		Iterator it3 = set3.iterator();

		int serialNumbOfTheEdge = 0;
		while (it3.hasNext()) {
			serialNumbOfTheEdge++;
			Map.Entry me = (Map.Entry) it3.next();
			System.out.print(me.getKey() + ": ");
			System.out.println(me.getValue());
			String inputStringEnteredByUser = (me.getValue()).toString();
			String[] parts = inputStringEnteredByUser.split(" InteractsWith ");
			String Interactor_A = parts[0]; //
			String Interactor_B = parts[1];

			int numOfVals = subgraph(Interactor_A, Interactor_B, "0", serialNumbOfTheEdge);
			if (numOfVals < kOfTheGeXAlgorithm) {
				approximateMatching(serialNumbOfTheEdge);
			}

			System.out.println("After approximate matching");

			String temp1, temp2;
			for (int j = 0; j < hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).size(); j++) {
				// System.out.println("Inside first if");
				for (int k = j + 1; k < hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge))
						.size(); k++) {
					String partsOfOne[] = hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).get(j)
							.split(" ");
					int objectOne = Integer.parseInt(partsOfOne[0]) + Integer.parseInt(partsOfOne[3]);

					String partsOfTwo[] = hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).get(k)
							.split(" ");
					int objectTwo = Integer.parseInt(partsOfTwo[0]) + Integer.parseInt(partsOfTwo[3]);
					if (objectOne > objectTwo) {
						temp1 = hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).get(j);
						temp2 = hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).get(k);
						hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).remove(k);
						hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).remove(j);

						System.out.println("Updated arraylist:");
						for (int v = 0; v < numOfVals - 2; v++) {
							System.out.println(
									hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).get(v));
						}

						hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).add(j, temp2);
						hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).add(k, temp1);
						// System.out.println("Exchanged");
						// System.out.println("Updated arraylist:");

						for (int u = 0; u < numOfVals; u++) {
							System.out.println(
									hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).get(u));
						}

					}
				}
			}
			System.out.println("The values inserted for edge" + serialNumbOfTheEdge + " :");
			for (int l = 0; l < numOfVals; l++) {
				System.out.println(hashmapUsedToGenerateOutput.get(String.valueOf(serialNumbOfTheEdge)).get(l));
			}

		}

	}

	/**
	 * approximateMatching uses the nodes which match the start and end nodes
	 * approximately. These nodes are then passed as arguments to the subgraph
	 * method to find all possible paths between the set of start and end nodes.
	 * 
	 * @param i
	 *            is the serial number of the input being considered
	 */
	public static void approximateMatching(int i) {
		// System.out.println("Inside approx matching");
		// input is the lists of matching nodes for the input entry being
		// considered
		ArrayList<String> listMatchingStartNodes = new ArrayList<String>();
		ArrayList<String> listMatchingEndNodes = new ArrayList<String>();
		listMatchingStartNodes = hashmapWithStartNodes.get(hashmapWithUserInput.get(i));
		listMatchingEndNodes = hashmapWithEndNodes.get(hashmapWithUserInput.get(i));
		// System.out.println("start" + listMatchingStartNodes.size());
		// System.out.println("end" + listMatchingEndNodes.size());
		for (int l = 0; l < listMatchingStartNodes.size(); l++) {
			for (int m = 0; m < listMatchingEndNodes.size(); m++) {
				// System.out.println("startValParts" +
				// listMatchingStartNodes.get(l) + "endValParts" +
				// listMatchingEndNodes.get(m));
				// Getting the starting point
				String startValParts[] = listMatchingStartNodes.get(l).split(" Lev: ");
				String startVal = startValParts[0];
				String LevDistStart = startValParts[1];

				//// Getting the ending point
				String endValParts[] = listMatchingEndNodes.get(m).split(" Lev: ");
				String endVal = endValParts[0];
				String LevDistEnd = endValParts[1];

				int TotalLev = Integer.parseInt(LevDistStart) + Integer.parseInt(LevDistEnd);
				String Lev = String.valueOf(TotalLev);

				subgraph(startVal, endVal, Lev, i);

			}
		}
		// output has to be (numOfHops,start NodeID,end NodeID,Lev dist)
	}

	/**
	 * The subgraph method finds the paths upto 5 hops between a pair of nodes
	 * whose Levenshtein distance is previous known. The path, the Levenshtein
	 * distance and the number of hops are then stored.
	 * 
	 * @param s1
	 *            is The first node.
	 * @param s2
	 *            is The second node.
	 * @param Lev
	 *            is The Levenshtein distance
	 * @param x
	 *            is the serial number of the input being considered
	 * @return
	 */
	public static int subgraph(String s1, String s2, String Lev, int x) {
		// The square brackets([]) at the start and end are removed using
		// regular expressions
		s1 = s1.replaceAll("\\[", "").replaceAll("\\]", "");
		s2 = s2.replaceAll("\\[", "").replaceAll("\\]", "");
		System.out.println("Checking for path between: " + s1 + " and " + s2);
		int numOfVals = 0;

		int resultOfQuery = 0;
		db = new GraphDatabaseFactory().newEmbeddedDatabase((neo4JFileAtDBPath));

		ArrayList<String> tempArrayList = new ArrayList<String>();
		/**
		 * For two given nodes, the matching paths are generated upto 5 hops
		 */
		for (int d = 0; d <= 5; d++) {
			String tempString = Integer.toString(d);
			try (Transaction ignored = db.beginTx();
					Result result = db.execute("MATCH p =(:Interactor{ Name:'" + s1 + "' })-[KNOWS*" + tempString
							+ "]-(:Interactor { Name: '" + s2 + "' }) RETURN p, length(p) LIMIT 10;")) {
				while (result.hasNext()) {
					// System.out.println("Entered while");
					Map<String, Object> row = result.next();

					{
						for (Entry<String, Object> column : row.entrySet()) {

							String s = row.values().toString();

							resultOfQuery++;
							int l = Character.getNumericValue((s.charAt(1)));
							// divide into 2 parts
							String parts[] = s.split("--");
							// get initial value
							String parts_2[] = parts[0].split(",");
							String st = parts_2[1];
							// Only digits are extracted by removing all nonn
							// digits
							st = st.replaceAll("\\D+", "");
							// System.out.println("source" + st);

							// get final value
							String str = parts[2 * l];
							str = str.replaceAll("\\D+", "");
							// System.out.println("destination" + str);

							String finalStr = s.charAt(1) + " " + st + " " + str + " " + Lev;
							;
							if (resultOfQuery % 2 == 1) {
								// System.out.println("Value being inserted:" +
								// row.values());
								// System.out.println(finalStr);
								tempArrayList.add(finalStr);
								numOfVals++;
							}

						}

						hashmapUsedToGenerateOutput.put(String.valueOf(x), tempArrayList);

					}

				}
				// System.out.println("Total values:" + numOfVals);
				ignored.terminate();
			}
		}
		db.shutdown();

		return numOfVals;

	}

	/**
	 * output is used to query the path of the output obtained using the scoring
	 * function. It prints out the top k paths approximately matching the user's
	 * input.
	 */
	public static void output() {
		String length = "";
		for (int y = 1; y <= numbOfInputs; y++) {
			System.out.println("Output matching edge:" + y + " " + hashmapWithUserInput.get(y));
			int totalvals = 0;
			// The minimum value between k and the values present in the output
			// array is chosen
			int sum = kOfTheGeXAlgorithm + 1;
			// System.out.println("HM
			// size:"+hashmapUsedToGenerateOutput.get(String.valueOf(y)).size());
			int minVal = Math.min(hashmapUsedToGenerateOutput.get(String.valueOf(y)).size(), (sum));
			innerloop: for (int i = 0; i < hashmapUsedToGenerateOutput.get(String.valueOf(y)).size(); i++) {

				if (totalvals < ((minVal))) {

					String outputString = hashmapUsedToGenerateOutput.get(String.valueOf(y)).get(i);
					String parts[] = outputString.split(" ");

					if (!length.equals(parts[0])) {
						length = parts[0];
						String start = parts[1];
						String end = parts[2];

						db = new GraphDatabaseFactory().newEmbeddedDatabase(neo4JFileAtDBPath);

						int skipper = 0;
						try (Transaction ignored3 = db.beginTx();
								Result result3 = db.execute("MATCH p =(s)-[KNOWS*" + length + "]-(t) where ID(s)="
										+ start + " and ID(t)=" + end + " RETURN p, length(p) LIMIT 10;")) {
							while (result3.hasNext() && (totalvals) < (kOfTheGeXAlgorithm)) {

								Map<String, Object> row = result3.next();

								{
									for (Entry<String, Object> column : row.entrySet()) {
										skipper++;

										if (skipper == 1) {
											System.out.println(row.values());
											// System.out.println(hashmapUsedToGenerateOutput.get(String.valueOf(y)).get(i));

											totalvals++;
											// System.out.println("totalvals"+totalvals);

										}
									}

								}

							}
							ignored3.terminate();

						}
						db.shutdown();
					}
				} else {

					break innerloop;

				}

			}

		}
	}

	/**
	 * createLevenshtein forms the query graph node labels from the inputs
	 * entered by the user. The data graph nodes having less than 4 levenshtein
	 * distance with the query node are collected.
	 */
	public static void createLevenshtein() {
		for (int i = 1; i <= numbOfInputs; i++) {
			String key = hashmapWithUserInput.get(i).toString();
			String[] parts = key.split(" InteractsWith ");
			String part1 = parts[0]; // 004
			String part2 = parts[1];

			db = new GraphDatabaseFactory().newEmbeddedDatabase(neo4JFileAtDBPath);
			ArrayList<String> temp1 = new ArrayList<String>();
			ArrayList<String> temp2 = new ArrayList<String>();
			try (Transaction ignored2 = db.beginTx(); Result result2 = db.execute("match (n) return n.Name;")) {
				while (result2.hasNext()) {

					Map<String, Object> row = result2.next();

					{
						for (Entry<String, Object> column : row.entrySet()) {

							String s = row.values().toString();

							if (findLevenshteinDistance(s, part1) < 4) {

								// The start nodes within the Levenshtein
								// distance are collected
								temp1.add(s + " Lev: " + String.valueOf(findLevenshteinDistance(s, part1)));
							}
							if (findLevenshteinDistance(s, part2) < 4) {
								// The end nodes within the Levenshtein distance
								// are collected
								temp2.add(s + " Lev: " + String.valueOf(findLevenshteinDistance(s, part2)));
							}
							hashmapWithStartNodes.put(key, temp1);
							hashmapWithEndNodes.put(key, temp2);
						}
					}
				}
				ignored2.terminate();
			}
			db.shutdown();

		}

	}
	// create 2 arraylists for each input
	// find lev dist below a value for 2 parts
	// find the subgraph for each and store the number of hops
	// Combine the lev and numb of hops and place in ascending order

	/**
	 * findMinimum is used to find the minimum value among given three values
	 * 
	 * @param a,
	 *            b and c are the three integers whose minimum value is being
	 *            found
	 * @return the minimum value among the arguments
	 */
	private static int findMinimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	/**
	 * findLevenshteinDistance is used to find the Levenshtein distance between
	 * two given strings.
	 * 
	 * @param string1
	 *            is the query node label
	 * @param string2
	 *            is the data node label
	 * @return the number of edits required to make them equal is returned
	 */
	public static int findLevenshteinDistance(String string1, String string2) {
		int[][] distance = new int[string1.length() + 1][string2.length() + 1];

		for (int i = 0; i <= string1.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= string2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= string1.length(); i++)
			for (int j = 1; j <= string2.length(); j++)
				distance[i][j] = findMinimum(distance[i - 1][j] + 1, distance[i][j - 1] + 1,
						distance[i - 1][j - 1] + ((string1.charAt(i - 1) == string2.charAt(j - 1)) ? 0 : 1));

		return distance[string1.length()][string2.length()];
	}

	public static void main(String[] args) {
		// take input from user
		askInputFromUser();

		// Find Levenshtein distance
		createLevenshtein();
		// Generate the subgraph
		generateSubgraph();

		// show the top k results
		output();

	}

}
