package br.ufes.inf.lprm.scene.mqc;

import org.kie.api.runtime.rule.AccumulateFunction;

import java.io.*;

/**
 * Created by pereirazc on 06/11/16.
 */
public class VarianceAccumulateFunction implements AccumulateFunction {
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }

    public void writeExternal(ObjectOutput out) throws IOException {

    }

    public static class VarianceData implements Externalizable {
        public Double last = null;
        public double current = 0;

        public VarianceData() {}

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            last = (Double) in.readObject();
            current   = in.readDouble();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(last);
            out.writeDouble(current);
        }

    }

    /* (non-Javadoc)
     * @see org.kie.base.accumulators.AccumulateFunction#createContext()
     */
    public Serializable createContext() {
        return new VarianceData();
    }

    /* (non-Javadoc)
     * @see org.kie.base.accumulators.AccumulateFunction#init(java.lang.Object)
     */
    public void init(Serializable context) throws Exception {
        VarianceData data = (VarianceData) context;
        data.current = 0;
    }

    /* (non-Javadoc)
     * @see org.kie.base.accumulators.AccumulateFunction#accumulate(java.lang.Object, java.lang.Object)
     */
    public void accumulate(Serializable context, Object value) {
        VarianceData data = (VarianceData) context;
        if (data.last != null) {
            data.current = data.current + ( ((Number) value).doubleValue() - data.last.doubleValue() );
        }
        data.last = new Double(((Number) value).doubleValue());
    }

    /* (non-Javadoc)
     * @see org.kie.base.accumulators.AccumulateFunction#reverse(java.lang.Object, java.lang.Object)
     */
    public void reverse(Serializable context,
                        Object value) throws Exception {
        VarianceData data = (VarianceData) context;

        if (data.last != null) {
            data.current = data.current + ( ((Number) value).doubleValue() - data.last.doubleValue() );
            data.last = new Double(((Number) value).doubleValue());
        }


    }

    /* (non-Javadoc)
     * @see org.kie.base.accumulators.AccumulateFunction#getResult(java.lang.Object)
     */
    public Object getResult(Serializable context) throws Exception {
        VarianceData data = (VarianceData) context;
        return new Double( data.current );
    }

    /* (non-Javadoc)
     * @see org.kie.base.accumulators.AccumulateFunction#supportsReverse()
     */
    public boolean supportsReverse() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Class< ? > getResultType() {
        return Double.class;
    }
}
