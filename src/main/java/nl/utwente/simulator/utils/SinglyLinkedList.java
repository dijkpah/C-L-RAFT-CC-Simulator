package nl.utwente.simulator.utils;

import java.util.Iterator;

public class SinglyLinkedList<E> implements Iterable<E> {

    private int size = 0;

    //fill with dummies
    private Node<E> pre;
    private PostNode<E> post;

    public E head(){
        return pre.next.value;
    }

    public SinglyLinkedList(){
        pre = new Node(null);
        post = new PostNode(null);
        pre.next = post;
        post.prev = pre;
    }

    public int size() {
        return size;
    }

    public void add(E e) {
        Node<E> newNode = new Node<E>(e);
        post.prev.next = newNode;
        newNode.next = post;
        post.prev = newNode;
        size++;
    }

    public void addAll(SinglyLinkedList<E> list) {
        post.prev.next = list.pre.next;
        post.prev = list.post.prev;
        size+=list.size;
    }


    @Override
    public Iterator<E> iterator() {
        return new SinglyLinkedListIterator<>();
    }

    private class SinglyLinkedListIterator<E> implements Iterator<E>{

        private Node<E> current = (Node<E>) pre.next;

        @Override
        public boolean hasNext() {
            return !(current.next instanceof PostNode);
        }

        @Override
        public E next() {
            E e = current.value;
            current = current.next;
            return e;
        }
    }

    private class Node<E> {
        private Node<E> next;
        private E value;

        public Node(E e){
            value=e;
        }
    }

    private class PostNode<E> extends Node<E> {
        private Node<E> prev;

        public PostNode(E e) {
            super(e);
        }
    }
}
