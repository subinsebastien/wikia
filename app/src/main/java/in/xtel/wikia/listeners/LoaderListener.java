package in.xtel.wikia.listeners;

/**
 * Created by napster on 10/02/15.
 */
public interface LoaderListener {
    /**
     * Called by the adapter class to notify Home class
     * that further loading is required. ItemCount is used
     * to claculate batch number
     * @param itemCount
     */
    public void onLoadMoreRequested(int itemCount);
}
