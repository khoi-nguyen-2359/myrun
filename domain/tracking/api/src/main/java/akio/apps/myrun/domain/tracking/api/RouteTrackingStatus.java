package akio.apps.myrun.domain.tracking.api;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

@IntDef({ RouteTrackingStatus.STOPPED, RouteTrackingStatus.RESUMED, RouteTrackingStatus.PAUSED })
@Retention(SOURCE)
@Target({ ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.TYPE_USE })
public @interface RouteTrackingStatus {
    public static final int STOPPED = 0;
    public static final int RESUMED = 1;
    public static final int PAUSED = 2;
}
