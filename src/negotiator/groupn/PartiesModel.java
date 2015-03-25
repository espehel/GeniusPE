package negotiator.groupn;

import negotiator.AgentID;
import negotiator.issue.Issue;
import negotiator.issue.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by espen on 25/03/15.
 */
public class PartiesModel {
    public Map<Issue, Map<Value, Integer>> estimatedIssueWeight;


    public PartiesModel() {
        estimatedIssueWeight = new HashMap<Issue, Map<Value, Integer>>();
    }

    public void updateIssueWeight(Issue issue, Value value){

        if(!estimatedIssueWeight.containsKey(issue)) {
            estimatedIssueWeight.put(issue, new HashMap<Value, Integer>());
            estimatedIssueWeight.get(issue).put(value, 1);
        }
        else {
            if(!estimatedIssueWeight.get(issue).containsKey(value)) {
                estimatedIssueWeight.get(issue).put(value, 1);
            }

            int weight = estimatedIssueWeight.get(issue).get(value);
            estimatedIssueWeight.get(issue).put(value, weight + 1);
        }
    }
    public double getWeight(Issue issue, Value value){
        return estimatedIssueWeight.get(issue).get(value);
    }
}
