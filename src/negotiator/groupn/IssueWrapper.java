package negotiator.groupn;

import negotiator.issue.Issue;

/**
 * Created by espen on 25/03/15.
 */
public class IssueWrapper {
    public Issue issue;
    public int id;
    public double weight;

    public IssueWrapper(int id, Issue issue, double weight) {
        this.id = id;
        this.issue = issue;
        this.weight = weight;

    }
}
