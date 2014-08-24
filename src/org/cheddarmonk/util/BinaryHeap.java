package org.cheddarmonk.util;

/**
 * @param <E> The type of elements in the heap.
 * @param <W> The type of the priorities / weights / costs.
 */
public class BinaryHeap<E, W extends Comparable<W>>
{
	@SuppressWarnings("unchecked")
	private Node<E, W>[] heap = new Node[128];
	private int size = 0;

	public void insert(E userData, W cost) {
		Node<E, W> node = new Node<E, W>();
		node.userData = userData;
		node.cost = cost;

		// Expand if necessary.
		if (size == heap.length) {
			@SuppressWarnings("unchecked")
			Node<E, W>[] newHeap = new Node[size << 1];
			System.arraycopy(heap, 0, newHeap, 0, size);
			heap = newHeap;
		}

		// Insert at end and bubble up.
		heap[size] = node;
		node.heapIdx = size;
		upHeap(size++);
	}

	public E pop() {
		if (size == 0) throw new IllegalStateException();

		Node<E, W> popped = heap[0];
		heap[0] = heap[--size];
		heap[size] = null;
		if (size > 0) downHeap(0);

		return popped.userData;
	}

	private void upHeap(int idx) {
		Node<E, W> node = heap[idx];
		W cost = node.cost;
		while (idx > 0) {
			int parentIdx = (idx - 1) >> 1;
			Node<E, W> parent = heap[parentIdx];
			if (cost.compareTo(parent.cost) < 0) {
				heap[idx] = parent;
				parent.heapIdx = idx;
				idx = parentIdx;
			}
			else break;
		}
		heap[idx] = node;
		node.heapIdx = idx;
	}

	private void downHeap(int idx) {
		Node<E, W> node = heap[idx];
		W cost = node.cost;

		while (true) {
			int leftIdx = 1 + (idx << 1);
			int rightIdx = leftIdx + 1;

			if (leftIdx >= size) break;

			// We definitely have a left child.
			Node<E, W> leftNode = heap[leftIdx];
			W leftCost = leftNode.cost;
			// We may have a right child.
			Node<E, W> rightNode;
			W rightCost;

			if (rightIdx >= size) {
				// Only need to compare with left.
				rightNode = null;
				rightCost = null;
			}
			else {
				rightNode = heap[rightIdx];
				rightCost = rightNode.cost;
			}

			// Find the smallest of the three costs: the corresponding node
			// should be the parent.
			if (rightCost == null || leftCost.compareTo(rightCost) < 0) {
				if (leftCost.compareTo(cost) < 0) {
					heap[idx] = leftNode;
					leftNode.heapIdx = idx;
					idx = leftIdx;
				}
				else break;
			}
			else if (rightCost.compareTo(cost) < 0) {
				heap[idx] = rightNode;
				rightNode.heapIdx = idx;
				idx = rightIdx;
			}
			else break;
		}

		heap[idx] = node;
		node.heapIdx = idx;
	}

	/**
	 * We assume that the user will be sensible with this! The design is aimed
	 * at people who have common sense and want efficiency.
	 */
	private static class Node<E, W>
	{
		/*package*/ W cost;
		/*package*/ int heapIdx;
		/*package*/ E userData;
	}
}
