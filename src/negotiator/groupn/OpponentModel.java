package negotiator.groupn;

import negotiator.AgentID;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.qualitymeasures.ScenarioInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by espen on 25/03/15.
 */
public class OpponentModel {
    public AgentID id;
    public Map<Issue, Map<Value, Double>> estimatedIssueWeight;


    public OpponentModel(AgentID agentID) {
        id = agentID;
        estimatedIssueWeight = new HashMap<Issue, Map<Value, Double>>();
    }

    public void updateIssueWeight(Issue issue, Value value){

        if(!estimatedIssueWeight.containsKey(issue)) {
            estimatedIssueWeight.put(issue, new HashMap<Value, Double>());
            estimatedIssueWeight.get(issue).put(value, Double.valueOf(1));
        }
        else {
            if(!estimatedIssueWeight.get(issue).containsKey(value)) {
                estimatedIssueWeight.get(issue).put(value, Double.valueOf(1));
            }

            double weight = estimatedIssueWeight.get(issue).get(value);
            estimatedIssueWeight.get(issue).put(value, weight + 1);
        }
    }
    public double getWeight(Issue issue, Value value){
        return estimatedIssueWeight.get(issue).get(value);
    }
}
