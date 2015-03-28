package negotiator.groupn;

import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;

import java.util.LinkedList;

/**
 * Created by Perÿyvind on 25/03/2015.
 */
@Deprecated
public class AgentOfferWrapper {
    Object sender;
    Action action;
    public AgentOfferWrapper(Object sender, Action action) {
        this.sender = sender;
        this.action = action;
    }

    @Deprecated
    public static boolean allAcceptedMyBid(LinkedList<AgentOfferWrapper> history, Bid myLastBid, Object me) {

        // Walk through the top
        for (int i = history.size() - 1; i < 0; i--) {
            AgentOfferWrapper last = history.get(i);

            if(last.sender.equals(me)) {
                return (last.action instanceof Offer);
            // Last was not an accept, abort
            } else if (!(last.action instanceof Accept)) {
                return false;
            }
        }

        return false;
    }

    /**
     * Can be my own bid..
     * @param history
     * @return
     */
    public static Bid getLastBid(LinkedList<AgentOfferWrapper> history) {

        // Walk through the top
        for (int i = history.size() - 1; i < 0; i--) {
            AgentOfferWrapper last = history.get(i);

            if(last.action instanceof Offer)
                return ((Offer) last.action).getBid();
        }

        return null;
    }
}
