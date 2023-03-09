package edu.hm.hafner.coverage;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import edu.hm.hafner.coverage.Coverage.CoverageBuilder;

/**
 * Class which represents a mutation of the PIT Mutation Testing tool.
 *
 * @author Melissa Bauer
 */
@SuppressWarnings("PMD.DataClass")
// TODO: intern some strings after deserialization to improve the memory footprint
public final class Mutation implements Serializable {
    private static final long serialVersionUID = -7725185756332899065L;

    private final boolean detected;
    private final MutationStatus status;
    private final int line;
    private final String mutator;
    private final String killingTest;
    private final String mutatedClass;
    private final String method;
    private final String signature;
    private final String description;

    @SuppressWarnings("checkstyle:ParameterNumber")
    private Mutation(final boolean detected, final MutationStatus status, final int line, final String mutator,
            final String killingTest, final String mutatedClass,
            final String method, final String signature, final String description) {
        this.detected = detected;
        this.status = status;
        this.line = line;
        this.mutator = mutator;
        this.killingTest = killingTest;
        this.mutatedClass = mutatedClass;
        this.method = method;
        this.signature = signature;
        this.description = description;
    }

    public String getMutatedClass() {
        return mutatedClass;
    }

    public String getMethod() {
        return method;
    }

    public String getSignature() {
        return signature;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDetected() {
        return detected;
    }

    public MutationStatus getStatus() {
        return status;
    }

    public boolean isValid() {
        return isCovered() || isMissed();
    }

    public boolean isCovered() {
        return status.isCovered();
    }

    public boolean isMissed() {
        return status.isMissed();
    }

    public int getLine() {
        return line;
    }

    public String getMutator() {
        return mutator;
    }

    public String getKillingTest() {
        return killingTest;
    }

    /**
     * Returns if the mutation was killed.
     *
     * @return if the mutation was killed
     */
    public boolean isKilled() {
        return status.equals(MutationStatus.KILLED);
    }

    /**
     * Returns if the mutation has survived.
     *
     * @return if the mutation has survived
     */
    public boolean hasSurvived() {
        return status.equals(MutationStatus.SURVIVED);
    }

    @Override
    public String toString() {
        return "[Mutation]:"
                + " isDetected=" + detected
                + ", status=" + status
                + ", lineNumber=" + line
                + ", mutator=" + mutator
                + ", killingTest='" + killingTest + "'";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Mutation mutation = (Mutation) o;
        return detected == mutation.detected && line == mutation.line && status == mutation.status
                && Objects.equals(mutator, mutation.mutator) && Objects.equals(killingTest,
                mutation.killingTest) && Objects.equals(mutatedClass, mutation.mutatedClass)
                && Objects.equals(method, mutation.method) && Objects.equals(signature,
                mutation.signature) && Objects.equals(description, mutation.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(detected, status, line, mutator, killingTest, mutatedClass, method, signature,
                description);
    }

    /**
     * Builder to create new {@link Mutation} instances.
     */
    @SuppressWarnings({"checkstyle:MissingJavadocMethod", "UnusedReturnValue"})
    public static class MutationBuilder {
        private boolean isDetected;
        private MutationStatus status = MutationStatus.NO_COVERAGE;
        private int line;
        private String mutator = StringUtils.EMPTY;
        private String killingTest = StringUtils.EMPTY;
        private String description = StringUtils.EMPTY;
        private String sourceFile = StringUtils.EMPTY;
        private String mutatedClass = StringUtils.EMPTY;
        private String mutatedMethod = StringUtils.EMPTY;
        private String mutatedMethodSignature = StringUtils.EMPTY;

        @CanIgnoreReturnValue
        public MutationBuilder setIsDetected(final boolean isDetected) {
            this.isDetected = isDetected;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setStatus(final MutationStatus status) {
            this.status = status;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setLine(final String line) {
            this.line = CoverageParser.parseInteger(line);

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setLine(final int line) {
            this.line = line;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setMutator(final String mutator) {
            this.mutator = mutator;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setKillingTest(final String killingTest) {
            this.killingTest = killingTest;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setDescription(final String description) {
            this.description = description;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setSourceFile(final String sourceFile) {
            this.sourceFile = sourceFile;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setMutatedClass(final String mutatedClass) {
            this.mutatedClass = mutatedClass;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setMutatedMethod(final String mutatedMethod) {
            this.mutatedMethod = mutatedMethod;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setMutatedMethodSignature(final String mutatedMethodSignature) {
            this.mutatedMethodSignature = mutatedMethodSignature;

            return this;
        }

        /**
         * Builds a new mutation and adds it to the root of the tree.
         *
         * @param root
         *         the module root to add the mutations to
         */
        public void buildAndAddToModule(final ModuleNode root) {
            String packageName = StringUtils.substringBeforeLast(mutatedClass, ".");
            String className = StringUtils.substringAfterLast(mutatedClass, ".");
            var packageNode = root.findPackage(packageName).orElseGet(() -> root.createPackageNode(packageName));
            var fileNode = packageNode.findFile(sourceFile).orElseGet(() -> packageNode.createFileNode(sourceFile));
            var classNode = fileNode.findClass(className).orElseGet(() -> fileNode.createClassNode(className));
            var methodNode = classNode.findMethod(mutatedMethod, mutatedMethodSignature)
                    .orElseGet(() -> classNode.createMethodNode(mutatedMethod, mutatedMethodSignature));

            var coverage = methodNode.getValue(Metric.MUTATION)
                    .map(Coverage.class::cast)
                    .orElse(Coverage.nullObject(Metric.MUTATION));
            var builder = new CoverageBuilder(coverage);
            if (isDetected) {
                builder.incrementCovered();
            }
            else {
                builder.incrementMissed();
            }
            methodNode.replaceValue(builder.build());
            fileNode.addMutation(build());
        }

        public Mutation build() {
            return new Mutation(isDetected, status, line, mutator, killingTest,
                    mutatedClass, mutatedMethod, mutatedMethodSignature, description);
        }
    }
}