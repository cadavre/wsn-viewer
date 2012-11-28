
package pl.cadavre.wsnv.entity;

import java.util.ArrayList;

import pl.cadavre.wsnv.exception.NonUniqueException;
import pl.cadavre.wsnv.type.Type;

public class UniqueResultSet extends ResultSet {

    private ArrayList<Result> results;

    /**
     * @return the results
     */
    public ArrayList<Result> getResults() {

        return results;
    }

    /**
     * @param results the results to set
     * @throws NonUniqueException
     */
    public void setResults(ArrayList<Result> results) throws NonUniqueException {

        this.results = results;

        if (!areResultsUnique()) {
            this.results.clear();

            throw new NonUniqueException();
        }
    }

    /**
     * @param result single result to add
     * @throws NonUniqueException
     */
    public void addResult(Result result) throws NonUniqueException {

        if (this.results == null) {
            this.results = new ArrayList<Result>();
        }

        this.results.add(result);

        if (!areResultsUnique()) {
            removeResult(result);

            throw new NonUniqueException();
        }
    }

    /**
     * @param result Result entity to remove
     * @return If element has been removed
     */
    public boolean removeResult(Result result) {

        return this.results.remove(result);
    }

    /**
     * @param result Index Result index to remove
     * @return Removed Result
     */
    public Result removeResult(int resultIndex) {

        return this.results.remove(resultIndex);
    }

    /**
     * @param result
     * @return If ResultSet contains result
     */
    public boolean contains(Result result) {

        return this.results.contains(result);
    }

    /**
     * @return If contained Results are unique (one each of a Type)
     */
    public boolean areResultsUnique() {

        for (Result resultParent : this.results) {
            for (Result resultChild : this.results) {
                if (resultParent != resultChild) {
                    if (resultParent.getType() == resultChild.getType()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * @param type
     * @return Result of provided Type
     */
    public Result getResultOfType(Type type) {

        for (Result result : this.results) {

            if (result.getType() == type) {
                return result;
            }
        }

        return null;
    }

}
