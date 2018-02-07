package tez2.persona;

import org.joda.time.LocalTime;
import scala.tools.nsc.backend.icode.Members;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suat on 07-Feb-18.
 */
public class ActionPlan {
    private List<JitaiTimeRange> jitaiTimeRanges = new ArrayList<>();

    public List<JitaiTimeRange> getJitaiTimeRanges() {
        return jitaiTimeRanges;
    }

    public void addJITAITimeRange(JitaiTimeRange jtr) {
        jitaiTimeRanges.add(jtr);
    }

    public static class JitaiTimeRange {
        private LocalTime startTime;
        private LocalTime endTime;
        private JitaiNature jitaiNature;

        public JitaiTimeRange(LocalTime startTime, LocalTime endTime, JitaiNature jitaiNature) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.jitaiNature = jitaiNature;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public JitaiNature getJitaiNature() {
            return jitaiNature;
        }
    }

    public enum JitaiNature {
        REMINDER, MOTIVATION
    }
}
