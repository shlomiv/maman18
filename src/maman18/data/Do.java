package maman18.data;

/**
 * @author Shlomi.v
 *
 * @param <T> - the type of the parameter
 * a generic callback interface, act as a little lambda expression
 */
public interface Do<T> {
	public void action(T t);
}
