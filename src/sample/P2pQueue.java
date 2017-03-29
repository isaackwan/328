package sample;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by isaac on 3/29/17.
 */
public class P2pQueue extends PriorityBlockingQueue {
}
/*
class P2pPiece {
    public byte[] data;
    public int order;
    public P2pPiece(byte[] data, int order) {
        this.data = data;
        this.order = order;
    }
}

class P2pPieceComparator implements Comparator<P2pPiece>
{
    @Override
    public int compare(P2pPiece x, P2pPiece y)
    {
        // Assume neither string is null. Real code should
        // probably be more robust
        // You could also just return x.length() - y.length(),
        // which would be more efficient.
        if (x.length() < y.length())
        {
            return -1;
        }
        if (x.length() > y.length())
        {
            return 1;
        }
        return 0;
    }
}
*/