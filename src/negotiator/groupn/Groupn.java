package negotiator.groupn;

import java.util.*;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.DeadlineType;
import negotiator.Timeline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Inform;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.utility.UtilitySpace;

/**
 * This is your negotiation party.
 */
public class Groupn extends AbstractNegotiationParty {

    private List<Offer> offerHistory;
    private Offer currentOffer;
    private ArrayList<IssueWrapper> issues;
    private Map<AgentID,OpponentModel> opponentModels;
    private Map<Value,Double> valueWeights;
    /**
     * a model for the preferences for all the parties
     */
    private PartiesModel model;
    /**
     * the current bid that we try to offer
     */
    private Bid myBid;
    /**
     *a set off all values that we have proposed so far
     */
    private Set<Value> proposedValues;
    /**
     * History of all received Offers and Accepts in chronological order.
     */
    private LinkedList<AgentOfferWrapper> history;

    /**
     * Please keep this constructor. This is called by genius.
     *
     * @param utilitySpace Your utility space.
     * @param deadlines The deadlines set for this negotiation.
     * @param timeline Value counting from 0 (start) to 1 (end).
     * @param randomSeed If you use any randomization, use this seed for it.
     */
    public Groupn(UtilitySpace utilitySpace,
                  Map<DeadlineType, Object> deadlines,
                  Timeline timeline,
                  long randomSeed) {
        // Make sure that this constructor calls it's parent.
        super(utilitySpace, deadlines, timeline, randomSeed);

        history = new LinkedList<>();

        System.out.println("==START CONSTRUCTOR===");

        offerHistory = new ArrayList<>();
        issues = new ArrayList<>();

        //saves all the issues for this scenraio with its id and weight
        ArrayList<Issue> tempList = utilitySpace.getDomain().getIssues();
        for (int i = 0; i < tempList.size(); i++) {
            IssueWrapper issueWrapper = new IssueWrapper(i,tempList.get(i),utilitySpace.getWeight(i));
            issues.add(issueWrapper);
        }

        //link all values to our preference weight
        System.out.println("-------------ENTRIES----------------");
        for (Map.Entry entry : utilitySpace.getEvaluators()){
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }



        try {
            myBid = utilitySpace.getMaxUtilityBid();


            for (IssueWrapper issue : issues){
                proposedValues.add(myBid.getValue(issue.id));
            }

            opponentModels = new HashMap<>();
            model = new PartiesModel();

        } catch (Exception e) {
            e.printStackTrace();
        }



        //System.out.println(utilitySpace.getDomain());
        System.out.println("----------UTILITYSPACE--------------");
        System.out.println(utilitySpace);

        //for(Issue i : issues) System.out.println("Constructor: " + i.getDescription());
        //for(Issue i : issues) System.out.println("Constructor: " + i);

        System.out.println("==END CONSTRUCTOR===");
    }

    /**
     * Each round this method gets called and ask you to accept or offer. The first party in
     * the first round is a bit different, it can only propose an offer.
     *
     * @param validActions Either a list containing both accept and offer or only offer.
     * @return The chosen action.
     */
    @Override
    public Action chooseAction(List<Class> validActions) {
        try {
            System.out.println("==START===");
            for (Class c : validActions) System.out.println("chooseAction: " + c.getSimpleName());
            System.out.println("==END===");

            // if we are the first party, we make the optimal offer for us.
            if (!validActions.contains(Accept.class)) {
                return new Offer(myBid);
            }
            //if the current offer has a utility that is equal or better than our bid
            else if(utilitySpace.getUtility(currentOffer.getBid())>=utilitySpace.getUtility(myBid)){
                return new Accept();
            }
            //if enough time has elapsed we will concede our bid
            else if(timeline.getTime()>0.5){
                myBid = concedeBid();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Offer(myBid);
    }

    private Bid concedeBid() throws Exception{

        for(IssueWrapper i : issues) {
                Value value = myBid.getValue(i.id);
        }

        return null;
    }


    /**
     * All offers proposed by the other parties will be received as a message.
     * You can use this information to your advantage, for example to predict their utility.
     *
     * @param sender The party that did the action.
     * @param action The action that party did.
     */
    @Override
    public void receiveMessage(Object sender, Action action) {
        super.receiveMessage(sender, action);

        // Boring information
        if(action instanceof Inform){
            //((Inform) action).
            System.out.println("receiveMessage: Informed: " + action);
            return;
        }


        //Update the history
        history.add(new AgentOfferWrapper(sender, action));

        // Here you can listen to other parties' messagese
        if(action instanceof Offer){
            System.out.println("receiveMessage: Offer: " + action);

            currentOffer = (Offer) action;
            offerHistory.add(currentOffer);
            updateOpponentModel(currentOffer.getAgent(), currentOffer.getBid());

            updateModel(currentOffer.getBid());

            System.out.println("receiveMessage: Bid utility: " + getUtility(currentOffer.getBid()));

            currentOffer.getAgent();

        } else if (action instanceof Accept) {
            updateModel(currentOffer.getBid());
        }
    }

    private void updateModel(Bid bid) {
        for (Issue issue : bid.getIssues()) {
            try {
                model.updateIssueWeight(issue,bid.getValue(issue.getNumber()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateOpponentModel(final AgentID agentID, Bid currentBid){
        if(!opponentModels.keySet().contains(agentID))
            opponentModels.put(agentID, new OpponentModel(agentID));

        OpponentModel opponent = opponentModels.get(agentID);
        for (Issue issue :currentBid.getIssues()) {

            try {
                opponent.updateIssueWeight(issue,currentBid.getValue(issue.getNumber()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
