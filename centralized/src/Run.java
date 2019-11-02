import java.util.LinkedList;
import java.util.ListIterator;

public class Run {
	public static void main(String[] args) {
		LinkedList<Integer> l = new LinkedList<Integer>();
		l.add(1);
		l.add(2);
		l.add(3);
		l.add(4);
		l.add(5);
		l.add(6);
//		ListIterator<Integer> backwardIterator = l.listIterator(l.size());
//		int i = 0;
//		while (backwardIterator.hasPrevious() && i < 10) {
//			backwardIterator.add(7);
//			Integer prev = backwardIterator.previous();
//			backwardIterator.remove();
//			backwardIterator.previous();
//			
//			
//			i++;
//		}
//		System.out.println(l);
		ListIterator<Integer> forwardIterator = l.listIterator();
		
		while (forwardIterator.hasNext()) {
			forwardIterator.add(7);
			System.out.println(l);
//			forwardIterator.next();
			forwardIterator.previous();
			forwardIterator.remove();
			forwardIterator.next();
		}
		
	}
}
