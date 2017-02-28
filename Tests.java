import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

public class Tests {
	
		@Test
		public void jia_Double_Random_Test() {
			System.out.println("\n Test");
		Character alphabet[] = new Character[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't'};

			String alphabetStrings[] = new String[alphabet.length];
			for (int i = 0; i < alphabet.length; i++) {
				alphabetStrings[i] = (alphabet[i]).toString();
			}
			BPlusTree<Character, String> tree = new BPlusTree<Character, String>();
			int testCounter = 0;
			Character testArray[] = new Character[alphabet.length];
			while (testCounter < alphabet.length ){
				//System.out.println("Test" + testCounter);
				int i = (int)(Math.random()*alphabet.length); 
				if (testArray[i] != alphabet[i]){
					testArray[i] = alphabet[i];
					testCounter ++;
					System.out.println("\n  "+alphabet[i]+" is about to be added");
					tree.insert(alphabet[i], alphabetStrings[i]);
					Utils.printTree(tree);
					//throw new RuntimeException("ff");
				}
			}
			
			testCounter = 0;
			while (testCounter < alphabet.length ){
				//System.out.println("Test" + testCounter);
				int i = (int)(Math.random()*alphabet.length); 
				if (testArray[i] == alphabet[i]){
					testArray[i] = null;
					testCounter ++;
					System.out.println("\n  "+alphabet[i]+" is about to be deleted");
					tree.delete(alphabet[i]);
					Utils.printTree(tree);
					//throw new RuntimeException("ff");
				}
			}
		}
		
		// trivial insertion, deletion cases
		@Test
		public void testTrivial(){
			System.out.println("\n\n## Trivial Test ##");
			BPlusTree<Character, String> tree = new BPlusTree<Character, String>();
			
			System.out.println("\n# at beginning, root should be null");
			Utils.printTree(tree); // should be null
			
			// insert a-d
			System.out.println("\n# insert a,b,c,d, no split");
			tree.insert('a', "a is here");
			System.out.println("# search 'a'");
			System.out.println(tree.search('a'));
			tree.insert('b', "b");
			tree.insert('c', "c");
			tree.insert('d', "d");
			Utils.printTree(tree);
			
			// insert f
			System.out.println("\n# insert f, should split");
			tree.insert('f', "f");// should split
			Utils.printTree(tree);
			
			// search 'a' and 'z', 'a' should find, 'z' shouldn't
			System.out.println("\n# search 'a' and 'z', 'a' should find, 'z' shouldn't");
			System.out.println(tree.search('a')); // should return "a is here"
			System.out.println(tree.search('z')); // should return null
			
			// delete 'c'
			System.out.println("\n# delete 'c'");
			tree.delete('c');
			Utils.printTree(tree);
			
			// delete 'd' twice
			System.out.println("\n# delete 'd', should merge, root should be gone");
			tree.delete('d');
			Utils.printTree(tree);
			System.out.println("# delete 'd' again, nothing should happen");
			tree.delete('d');
			Utils.printTree(tree);
			
			// search 'd', should be null
			System.out.println("\n# search 'd', should return null");
			System.out.println(tree.search('d'));
			
			// delete 'a'
			System.out.println("\n# delete 'b', re-arrange a and f");
			tree.delete('b');
			Utils.printTree(tree);
			
			// delete 'a', the leaf is underflow but it's the root, so it's fine
			System.out.println("\n# delete 'b', the leaf becomes root, old root should be gone");
			tree.delete('a');
			Utils.printTree(tree);
			
			// delete 'f', the leaf AKA the root is empty, root should be set to null
			System.out.println("\n# delete 'f', root should be set to null");
			tree.delete('f');
			Utils.printTree(tree);
		}


	// add some nodes, see if it comes out right, delete one, see if it's right
	@Test
	public void testSimpleHybrid() {
		System.out.println("\n testSimpleHybrid");
		Character alphabet[] = new Character[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g' };
		String alphabetStrings[] = new String[alphabet.length];
		for (int i = 0; i < alphabet.length; i++) {
			alphabetStrings[i] = (alphabet[i]).toString();
		}
		BPlusTree<Character, String> tree = new BPlusTree<Character, String>();
		Utils.bulkInsert(tree, alphabet, alphabetStrings);

		String test = Utils.outputTree(tree);
		String correct = "@c/e/@%%[(a,a);(b,b);]#[(c,c);(d,d);]#[(e,e);(f,f);(g,g);]$%%";

		assertEquals(correct, test);

		tree.delete('a');

		test = Utils.outputTree(tree);
		correct = "@e/@%%[(b,b);(c,c);(d,d);]#[(e,e);(f,f);(g,g);]$%%";
		assertEquals(correct, test);

	}

	// add some nodes, see if it comes out right, delete one, see if it's right
	@Test
	public void testSimpleHybrid2() {
		Integer primeNumbers[] = new Integer[] { 2, 4, 5, 7, 8, 9, 10, 11, 12,
				13, 14, 15, 16 };
		String primeNumberStrings[] = new String[primeNumbers.length];
		for (int i = 0; i < primeNumbers.length; i++) {
			primeNumberStrings[i] = (primeNumbers[i]).toString();
		}
		BPlusTree<Integer, String> tree = new BPlusTree<Integer, String>();
		Utils.bulkInsert(tree, primeNumbers, primeNumberStrings);

		String test = Utils.outputTree(tree);
		String correct = "@10/@%%@5/8/@@12/14/@%%[(2,2);(4,4);]#[(5,5);(7,7);]#[(8,8);(9,9);]$[(10,10);(11,11);]#[(12,12);(13,13);]#[(14,14);(15,15);(16,16);]$%%";
		assertEquals(test, correct);

		tree.delete(2);
		test = Utils.outputTree(tree);
		Utils.printTree(tree);
		correct = "@8/10/12/14/@%%[(4,4);(5,5);(7,7);]#[(8,8);(9,9);]#[(10,10);(11,11);]#[(12,12);(13,13);]#[(14,14);(15,15);(16,16);]$%%";
		assertEquals(test, correct);
	}
	// add some nodes, delete one by one
		@Test
		public void testcase0() {
			Integer primeNumbers[] = new Integer[] { 2, 4, 5, 7, 8, 9, 10, 11, 12,
					13, 14, 15, 16 ,17, 18, 19, 23, 37, 38};
			String primeNumberStrings[] = new String[primeNumbers.length];
			for (int i = 0; i < primeNumbers.length; i++) {
				primeNumberStrings[i] = (primeNumbers[i]).toString();
			}
			BPlusTree<Integer, String> tree = new BPlusTree<Integer, String>();
			Utils.bulkInsert(tree, primeNumbers, primeNumberStrings);

			String test = Utils.outputTree(tree);
			String correct = "@10/16/@%%@5/8/@@12/14/@@18/23/@%%[(2,2);(4,4);]#[(5,5);(7,7);]#[(8,8);(9,9);]$[(10,10);(11,11);]#[(12,12);(13,13);]#[(14,14);(15,15);]$[(16,16);(17,17);]#[(18,18);(19,19);]#[(23,23);(37,37);(38,38);]$%%";
			assertEquals(test, correct);
               
			
		}
		@Test
		public void testcase1() {
			Integer primeNumbers[] = new Integer[] { 2, 4, 5, 7, 8};
			String primeNumberStrings[] = new String[primeNumbers.length];
			for (int i = 0; i < primeNumbers.length; i++) {
				primeNumberStrings[i] = (primeNumbers[i]).toString();
			}
			BPlusTree<Integer, String> tree = new BPlusTree<Integer, String>();
			Utils.bulkInsert(tree, primeNumbers, primeNumberStrings);

			String test = Utils.outputTree(tree);
			String correct = "@5/@%%[(2,2);(4,4);]#[(5,5);(7,7);(8,8);]$%%";
			assertEquals(test, correct);
           
			
			tree.delete(5);
			 test = Utils.outputTree(tree);
			 Utils.printTree(tree);
			correct = "@5/@%%[(2,2);(4,4);]#[(7,7);(8,8);]$%%";
			assertEquals(test, correct);
            
       }


	@Test
	public void testBookExampleShort() {
		Integer exampleNumbers[] = new Integer[] { 2, 3, 13, 14, 17, 19, 24, 27,
				30, 33, 34, 38, 5, 7, 16, 20, 22, 29 };
		String primeNumberStrings[] = new String[exampleNumbers.length];
		for (int i = 0; i < exampleNumbers.length; i++) {
			primeNumberStrings[i] = (exampleNumbers[i]).toString();
		}
		BPlusTree<Integer, String> tree = new BPlusTree<Integer, String>();
		Utils.bulkInsert(tree, exampleNumbers, primeNumberStrings);
		Utils.printTree(tree);
		tree.delete(13);
		tree.delete(17);
		tree.delete(30);
		tree.insert(39, "39");
		Utils.printTree(tree);
		// Initial tree
		String test = Utils.outputTree(tree);
		String correct = "@13/17/24/30/@%%[(2,2);(3,3);(5,5);(7,7);]#[(14,14);(16,16);]#[(19,19);(20,20);(22,22);]#[(24,24);(27,27);(29,29);]#[(33,33);(34,34);(38,38);(39,39);]$%%";
		assertEquals(test, correct);
	}

	// testing proper leaf node merging behaviour
	@Test
	public void testDeleteLeafNodeRedistribute() {
		Integer testNumbers[] = new Integer[] { 2, 4, 7, 8, 5, 6, 3 };
		String testNumberStrings[] = new String[testNumbers.length];
		for (int i = 0; i < testNumbers.length; i++) {
			testNumberStrings[i] = (testNumbers[i]).toString();
		}
		BPlusTree<Integer, String> tree = new BPlusTree<Integer, String>();
		Utils.bulkInsert(tree, testNumbers, testNumberStrings);
		tree.delete(6);
		tree.delete(7);
		tree.delete(8);
		String test = Utils.outputTree(tree);
		Utils.printTree(tree);

		String result = "@4/@%%[(2,2);(3,3);]#[(4,4);(5,5);]$%%";
		assertEquals(result, test);
	}

	// Testing appropriate depth and node invariants on a big tree
	@Test
	public void testLargeTree() {
		BPlusTree<Integer, Integer> tree = new BPlusTree<Integer, Integer>();
		ArrayList<Integer> numbers = new ArrayList<Integer>(100000);
		for (int i = 0; i < 100000; i++) {
			numbers.add(i);
		}
		Collections.shuffle(numbers);
		for (int i = 0; i < 100000; i++) {
			tree.insert(numbers.get(i), numbers.get(i));
		}
		testTreeInvariants(tree);

		assertTrue(treeDepth(tree.root) < 11);
	}

	public <K extends Comparable<K>, T> void testTreeInvariants(
			BPlusTree<K, T> tree) {
		for (Node<K, T> child : ((IndexNode<K, T>) (tree.root)).children)
			testNodeInvariants(child);
	}

	public <K extends Comparable<K>, T> void testNodeInvariants(Node<K, T> node) {
		assertFalse(node.keys.size() > 2 * BPlusTree.D);
		assertFalse(node.keys.size() < BPlusTree.D);
		if (!(node.isLeafNode))
			for (Node<K, T> child : ((IndexNode<K, T>) node).children)
				testNodeInvariants(child);
	}

	public <K extends Comparable<K>, T> int treeDepth(Node<K, T> node) {
		if (node.isLeafNode)
			return 1;
		int childDepth = 0;
		int maxDepth = 0;
		for (Node<K, T> child : ((IndexNode<K, T>) node).children) {
			childDepth = treeDepth(child);
			if (childDepth > maxDepth)
				maxDepth = childDepth;
		}
		return (1 + maxDepth);
	}
}
