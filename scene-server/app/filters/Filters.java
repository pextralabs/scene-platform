package filters;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.http.HttpFilters;
import play.mvc.EssentialAction;
import play.mvc.EssentialFilter;
import play.filters.cors.CORSFilter;

@Singleton
public class Filters extends EssentialFilter implements HttpFilters {

    @Inject
    private CORSFilter corsFilter;

    @Override
    public EssentialAction apply(EssentialAction next) {
        return corsFilter.asJava().apply(next);
    }

    @Override
    public EssentialFilter[] filters() {
        EssentialFilter[] result = new EssentialFilter[1];
        result[0] = this;
        return result;
    }
}