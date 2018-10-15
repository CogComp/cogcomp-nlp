/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wsim.wordnet;

public class Triple<L, M, R> {

	private L left;
	private M middle;
	private R right;

	public Triple(L left, M middle, R right) {
		if (left == null) {
			throw new IllegalArgumentException("Left value is not effective.");
		}
		if (middle == null) {
			throw new IllegalArgumentException("Middle value is not effective.");
		}
		if (right == null) {
			throw new IllegalArgumentException("Right value is not effective.");
		}
		this.left = left;
		this.middle = middle;
		this.right = right;
	}

	public L getLeft() {
		return this.left;
	}

	public M getMiddle() {
		return this.middle;
	}

	public R getRight() {
		return this.right;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((middle == null) ? 0 : middle.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Triple<Object, Object, Object> other = (Triple<Object, Object, Object>) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (middle == null) {
			if (other.middle != null)
				return false;
		} else if (!middle.equals(other.middle))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "<" + left + "," + middle + "," + right + ">";
	}

}
