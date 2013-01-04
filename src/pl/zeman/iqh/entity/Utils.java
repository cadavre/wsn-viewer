
package pl.zeman.iqh.entity;

import java.util.ArrayList;

/**
 * Utilities class for iterating in Node, Result and Health objects and its ArrayLists
 * 
 * @author Seweryn Zeman <seweryn.zeman@gmail.com>
 */
public class Utils {

    public static Node getNode(ArrayList<Node> nodes, int id) {

        for (Node node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }

        return null;
    }

    /**
     * Get Result object by Node object
     * 
     * @param results
     * @param node
     * @return Result
     */
    public static Result getResultForNode(ArrayList<Result> results, Node node) {

        return getResultForID(results, node.getId());
    }

    /**
     * Get Health object by Node object
     * 
     * @param healths
     * @param node
     * @return Health
     */
    public static Health getHealthForNode(ArrayList<Health> healths, Node node) {

        return getHealthForID(healths, node.getId());
    }

    /**
     * Get Result object by node ID
     * 
     * @param results
     * @param id
     * @return Result
     */
    public static Result getResultForID(ArrayList<Result> results, int id) {

        for (Result result : results) {
            if (result.getNodeId() == id) {
                return result;
            }
        }

        return null;
    }

    /**
     * Get Health object by node ID
     * 
     * @param healths
     * @param id
     * @return Health
     */
    public static Health getHealthForID(ArrayList<Health> healths, int id) {

        for (Health health : healths) {
            if (health.getNodeId() == id) {
                return health;
            }
        }

        return null;
    }
}
