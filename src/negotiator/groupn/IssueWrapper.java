package negotiator.groupn;

import negotiator.issue.Issue;
import negotiator.issue.ValueDiscrete;

import java.util.Set;

/**
 * Created by espen on 25/03/15.
 */
public class IssueWrapper {
    public Set<ValueDiscrete> values;
    public Issue issue;
    public int id;
    public double weight;

    public IssueWrapper(int id, Issue issue, double weight, Set<ValueDiscrete> values) {
        this.id = id;
        this.issue = issue;
        this.weight = weight;
        this.values = values;

    }


    @Override
    public String toString() {
        return "IW{" +
                "issue=" + issue +
                ", id=" + id +
                ", weight=" + weight +
                '}';
    }
}
