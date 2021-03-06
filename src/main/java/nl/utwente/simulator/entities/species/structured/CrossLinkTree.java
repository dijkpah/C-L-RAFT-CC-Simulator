package nl.utwente.simulator.entities.species.structured;


import javax.annotation.Nonnull;
import java.util.Iterator;

public class CrossLinkTree implements Iterable<CrossLink>{

    private CrossLinkTree left;
    private CrossLinkTree right;
    private final int offset;

    public CrossLinkTree(@Nonnull CrossLinkTree left, @Nonnull CrossLinkTree right, int offset) {
        this.left = left;
        this.right = right;
        this.offset = offset;
    }

    public CrossLinkTree(@Nonnull CrossLinkTree left, @Nonnull CrossLink value) {
        this.right = new Element(value);
        this.left = left;
        this.offset = 0;
    }

    private CrossLinkTree() {
        super();
        this.offset = 0;
    }

    @Override
    public Iterator<CrossLink> iterator() {
        return iterator(0);
    }

    public Iterator<CrossLink> iterator(int offsetSuper) {
        return new Iterator<CrossLink>() {
            final Iterator<? extends CrossLink> iterator1 = left.iterator(offsetSuper);
            final Iterator<? extends CrossLink> iterator2 = right.iterator(offsetSuper+offset);

            @Override
            public boolean hasNext() {
                return iterator1.hasNext() || iterator2.hasNext();
            }

            @Override
            public CrossLink next() {
                return iterator1.hasNext() ? iterator1.next() : iterator2.next();
            }
        };
    }

    public static class EMPTYLIST extends CrossLinkTree {
        @Override
        public Iterator<CrossLink> iterator() {
            Iterator<CrossLink> it = new Iterator<CrossLink>() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public CrossLink next() {
                    throw new NullPointerException();
                }
            };
            return it;
        }

        @Override
        public Iterator<CrossLink> iterator(int offsetSuper) {
            return this.iterator();
        }
    }

    private class Element extends CrossLinkTree {
        private CrossLink value;

        public CrossLink getValue() {
            return this.value;
        }

        Element(CrossLink value) {
            super();
            this.value = value;
        }

        @Override
        public Iterator<CrossLink> iterator() {
            Iterator<CrossLink> it = new Iterator<CrossLink>() {

                private boolean wasRead = false;

                @Override
                public boolean hasNext() {
                    return !wasRead;
                }

                @Override
                public CrossLink next() {
                    wasRead = true;
                    return value;
                }
            };
            return it;
        }

        @Override
        public Iterator<CrossLink> iterator(int offsetSuper) {
            Iterator<CrossLink> it = new Iterator<CrossLink>() {

                private boolean wasRead = false;

                @Override
                public boolean hasNext() {
                    return !wasRead;
                }

                @Override
                public CrossLink next() {
                    wasRead = true;
                    return new CrossLink(
                            new CrossLink.Pointer(value.firstHalf, offsetSuper),
                            new CrossLink.Pointer(value.secondHalf, offsetSuper)
                    );
                }
            };
            return it;
        }
    }

}
