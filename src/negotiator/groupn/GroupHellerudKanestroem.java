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
import negotiator.issue.*;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;
import negotiator.utility.UtilitySpace;
import org.jfree.data.statistics.StatisticalCategoryDataset;

/**
 * This is your negotiation party.
 */
public class GroupHellerudKanestroem extends AbstractNegotiationParty {

    public static double VALUE_PREFFERENCE_FACTOR = 10;
    public static double VALUE_GLOBAL_COUNT_FACTOR = 0.1;

    private List<Offer> offerHistory;
    private Offer currentOffer;
    private ArrayList<IssueWrapper> issues;
    private Map<AgentID,OpponentModel> opponentModels;
    private Map<Value,Double> valueWeights;
    private Random random;
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
    private Stack<Value> proposedValuesStack;

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
    public GroupHellerudKanestroem(UtilitySpace utilitySpace,
                                   Map<DeadlineType, Object> deadlines,
                                   Timeline timeline,
                                   long randomSeed) {
        // Make sure that this constructor calls it's parent.
        super(utilitySpace, deadlines, timeline, randomSeed);

        random = new Random(randomSeed);
        history = new LinkedList<>();

        System.out.println("==START CONSTRUCTOR===");

        offerHistory = new ArrayList<>();
        issues = new ArrayList<>();

        //saves all the issues for this scenario with its id and weight
        ArrayList<Issue> tempList = utilitySpace.getDomain().getIssues();
        for (int i = 0; i < tempList.size(); i++) {
            // +1 to fix indexing..
            IssueWrapper issueWrapper = new IssueWrapper(i+1,tempList.get(i),utilitySpace.getWeight(i+1),((EvaluatorDiscrete) utilitySpace.getEvaluator(i+1)).getValues());
            issues.add(issueWrapper);
        }
        try {
            //link all values to our preference weight
            valueWeights = new HashMap<>();
            for (IssueWrapper issue : issues){
                if(issue.issue.getType() == ISSUETYPE.DISCRETE){
                    EvaluatorDiscrete evaluator = (EvaluatorDiscrete) utilitySpace.getEvaluator(issue.id);
                    for (ValueDiscrete value : evaluator.getValues()){
                        //the weight for a value multiplied with the weight of its issue
                        double weight = evaluator.getEvaluation(value)*evaluator.getWeight();
                        valueWeights.put(value, weight);
                    }
                }
            }

            //instantiates myBid with the max utility
            myBid = utilitySpace.getMaxUtilityBid();

            //initiates the set with the values from our maxbid
            proposedValues = new HashSet<>();
            proposedValuesStack = new Stack<>();

            for (IssueWrapper issue : issues){
                proposedValues.add(myBid.getValue(issue.id));
                proposedValuesStack.push(myBid.getValue(issue.id));
            }

            opponentModels = new HashMap<>();
            model = new PartiesModel();

        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("----------UTILITYSPACE--------------");
        System.out.println(utilitySpace);
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
            else if(timeline.getTime() > 0.5){
                myBid = concedeBidEspen();
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

    private Bid concedeBidEspen() throws Exception{

        List<Value> bestValues = new ArrayList<>();
        double bestScore = 0;

        for(Value value : valueWeights.keySet()) {
            if(proposedValuesStack.contains(value))
                continue;

            double valueScore = valueWeights.get(value) * VALUE_PREFFERENCE_FACTOR;

            valueScore += model.getWeight(findIssueByValue(value).issue, value) * VALUE_GLOBAL_COUNT_FACTOR;

            if(valueScore > bestScore){
                bestValues.clear();
                bestValues.add(value);
                bestScore=valueScore;
            } else if(valueScore == bestScore){
                bestValues.add(value);
            }
        }

        Value lastProposed;

        //makes sure that we only concede on one issue before 0.9 time has passed
        if(timeline.getTime() < 0.9) {
            lastProposed = proposedValuesStack.pop();
            myBid = utilitySpace.getMaxUtilityBid();
        }

        // todo Her kommer mitt forslag inn
        // Vi velger den verdien som påvirker oss minst.
        Value valueToConcede = bestValues.get(random.nextInt(bestValues.size()));
        proposedValuesStack.push(valueToConcede);

        Bid newBid = new Bid(myBid);
        newBid.setValue(findIssueByValue(valueToConcede).id, valueToConcede);

        return newBid;
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
            System.out.println("receiveMessage: Informed: " + action);
            return;
        }

        //Update the history
        history.add(new AgentOfferWrapper(sender, action));

        // Here you can listen to other parties' messagese
        if(action instanceof Offer){
            System.out.println("receiveMessage: Offer: " + action);

            currentOffer = (Offer)action;

            offerHistory.add(currentOffer);

            updateOpponentModel(currentOffer.getAgent(), currentOffer.getBid());
            updateModel(currentOffer.getBid());

            System.out.println("receiveMessage: Bid utility: " + getUtility(currentOffer.getBid()));

        } else if (action instanceof Accept) {
            updateModel(currentOffer.getBid());
        }
    }

    private void updateModel(Bid bid) {
        if(bid != null) {
            for (Issue issue : bid.getIssues()) {
                try {
                    model.updateIssueWeight(issue, bid.getValue(issue.getNumber()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            System.out.println("ERROR: CURRENT BID IS NULL");
        }
    }

    public void updateOpponentModel(final AgentID agentID, Bid currentBid){
        if(!opponentModels.keySet().contains(agentID))
            opponentModels.put(agentID, new OpponentModel(agentID));

        OpponentModel opponent = opponentModels.get(agentID);

        if(currentBid != null) {
            for (Issue issue : currentBid.getIssues()) {
                try {
                    opponent.updateIssueWeight(issue, currentBid.getValue(issue.getNumber()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            System.out.println("ERROR: CURRENT BID IS NULL");
        }
    }

    public IssueWrapper findIssueByValue(Value value){
        for (IssueWrapper issue : issues){
            for (Value iValue : issue.values){
                if(value.equals(iValue))
                    return issue;
            }
        }

        return null;
    }
}
