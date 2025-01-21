package com.seanrossharvey.srpn;

import java.util.ArrayDeque;

/**
 * A class that implements a stack data structure (LIFO (last in, first out)) using a
 * {@link java.util.ArrayDeque}.<br>
 * This stack specifically stores elements of type {@code Long}.
 * This class provides methods for push, pop, and peek for elements in the stack, stack size return, and stack printing.
 */
public final class Stack {
    /**
     * The underlying {@link java.util.ArrayDeque} used to implement the stack.
     */
    private final ArrayDeque<Long> stack = new ArrayDeque<>();

    /**
     * Pushes an element onto the top of the stack.
     * @param element the element to be pushed onto the stack.
     * @throws NullPointerException if the specified element is null.
     */
    public void push(Long element) {
        stack.push(element);
    }

    /**
     * Removes and returns the top element from the stack (ordered).
     * @return the top element from the stack.
     * @throws java.util.NoSuchElementException if the stack is empty.
     */
    public Long pop() {
        return stack.pop();
    }

    /**
     * Returns the element at the top of the stack without removal (stack remains unmodified).
     * @return the element at the top of the stack, or null if the stack is empty (no elements).
     * @see ArrayDeque#peek()
     */
    public Long peek() {
        return stack.peek();
    }

    /**
     * Returns the number of elements in the stack.
     * @return the number of elements in the stack.
     */
    public int size() {
        return stack.size();
    }

    /**
     * Prints elements in the stack from top to bottom.
     */
    public void printStack() {
        if (stack.isEmpty()) {
            System.out.println(Integer.MIN_VALUE);
        } else {
            stack.descendingIterator().forEachRemaining(System.out::println);
        }
    }

	public void clear() {
		stack.clear();
	}
}
