/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.fabric.protocol;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Message;

/**
 * Immutable protocol checkpoint execution pipeline.
 * <p>
 * A pipeline is a real, ordered execution chain. Each stage receives the current message and a downstream chain. A
 * stage may transform the message, stop the chain by returning without calling {@code proceed}, or call {@code proceed}
 * exactly once to continue. Terminal checkpoints execute their own stage and then prevent any later stages from
 * running.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Pipeline {

    /**
     * Identity stage processor.
     */
    private static final Processor IDENTITY = (message, chain) -> chain.proceed(message);

    /**
     * Ordered protocol stages.
     */
    private final List<Stage> stages;

    /**
     * Creates a protocol pipeline.
     *
     * @param stages stages
     */
    private Pipeline(final List<Stage> stages) {
        this.stages = List.copyOf(validateStages(stages));
    }

    /**
     * Creates a protocol pipeline with identity processors for each checkpoint.
     *
     * @param checkpoints checkpoints
     * @return protocol pipeline
     */
    public static Pipeline of(final List<Checkpoint> checkpoints) {
        return new Pipeline(identityStages(checkpoints));
    }

    /**
     * Creates a protocol pipeline from executable stages.
     *
     * @param stages executable stages
     * @return protocol pipeline
     */
    public static Pipeline ofStages(final List<Stage> stages) {
        return new Pipeline(stages);
    }

    /**
     * Creates an executable stage.
     *
     * @param checkpoint checkpoint boundary
     * @param processor  stage processor
     * @return executable stage
     */
    public static Stage stage(final Checkpoint checkpoint, final Processor processor) {
        return new Stage(checkpoint, processor);
    }

    /**
     * Executes this pipeline.
     *
     * @param message message
     * @return message
     */
    public Message execute(final Message message) {
        return executeAt(0, validateMessage(message));
    }

    /**
     * Executes this pipeline.
     *
     * @param message message
     * @return message
     */
    public Message proceed(final Message message) {
        return execute(message);
    }

    /**
     * Returns a new pipeline with an appended checkpoint.
     *
     * @param checkpoint checkpoint
     * @return new pipeline
     */
    public Pipeline add(final Checkpoint checkpoint) {
        return add(stage(checkpoint, IDENTITY));
    }

    /**
     * Returns a new pipeline with an appended stage.
     *
     * @param stage executable stage
     * @return new pipeline
     */
    public Pipeline add(final Stage stage) {
        validateStage(stage);
        final ArrayList<Stage> copy = new ArrayList<>(stages);
        copy.add(stage);
        return new Pipeline(copy);
    }

    /**
     * Returns checkpoint snapshot.
     *
     * @return checkpoints
     */
    public List<Checkpoint> checkpoints() {
        final ArrayList<Checkpoint> result = new ArrayList<>(stages.size());
        for (final Stage stage : stages) {
            result.add(stage.checkpoint());
        }
        return List.copyOf(result);
    }

    /**
     * Returns executable stage snapshot.
     *
     * @return stages
     */
    public List<Stage> stages() {
        return List.copyOf(stages);
    }

    /**
     * Executes the stage at an index.
     *
     * @param index   stage index
     * @param message current message
     * @return resulting message
     */
    private Message executeAt(final int index, final Message message) {
        if (index >= stages.size()) {
            return message;
        }
        final Stage stage = stages.get(index);
        final Chain chain = new ExecutionChain(stage.checkpoint(), index + 1, stage.checkpoint().terminal());
        return validateMessage(stage.processor().process(message, chain));
    }

    /**
     * Creates identity stages for checkpoint-only pipelines.
     *
     * @param checkpoints checkpoints
     * @return executable stages
     */
    private static List<Stage> identityStages(final List<Checkpoint> checkpoints) {
        final List<Checkpoint> validated = validateCheckpoints(checkpoints);
        final ArrayList<Stage> result = new ArrayList<>(validated.size());
        for (final Checkpoint checkpoint : validated) {
            result.add(stage(checkpoint, IDENTITY));
        }
        return result;
    }

    /**
     * Validates checkpoint list.
     *
     * @param checkpoints checkpoints
     * @return checkpoints
     */
    private static List<Checkpoint> validateCheckpoints(final List<Checkpoint> checkpoints) {
        final List<Checkpoint> checked = Assert
                .notNull(checkpoints, () -> new ValidateException("Pipeline checkpoints must not be null"));
        boolean terminal = false;
        for (final Checkpoint checkpoint : checked) {
            validateCheckpoint(checkpoint);
            if (checkpoint.terminal()) {
                Assert.isFalse(
                        terminal,
                        () -> new ProtocolException("Pipeline must not contain duplicate terminal checkpoints"));
                terminal = true;
            }
        }
        return checked;
    }

    /**
     * Validates stage list.
     *
     * @param stages stages
     * @return stages
     */
    private static List<Stage> validateStages(final List<Stage> stages) {
        final List<Stage> checked = Assert
                .notNull(stages, () -> new ValidateException("Pipeline stages must not be null"));
        boolean terminal = false;
        for (final Stage stage : checked) {
            validateStage(stage);
            if (stage.checkpoint().terminal()) {
                Assert.isFalse(
                        terminal,
                        () -> new ProtocolException("Pipeline must not contain duplicate terminal checkpoints"));
                terminal = true;
            }
        }
        return checked;
    }

    /**
     * Validates a stage.
     *
     * @param stage stage
     * @return stage
     */
    private static Stage validateStage(final Stage stage) {
        return Assert.notNull(stage, () -> new ValidateException("Pipeline stage must not be null"));
    }

    /**
     * Validates a checkpoint.
     *
     * @param checkpoint checkpoint
     * @return checkpoint
     */
    private static Checkpoint validateCheckpoint(final Checkpoint checkpoint) {
        return Assert.notNull(checkpoint, () -> new ValidateException("Pipeline checkpoint must not be null"));
    }

    /**
     * Validates a processor.
     *
     * @param processor processor
     * @return processor
     */
    private static Processor validateProcessor(final Processor processor) {
        return Assert.notNull(processor, () -> new ValidateException("Pipeline processor must not be null"));
    }

    /**
     * Validates a message.
     *
     * @param message message
     * @return message
     */
    private static Message validateMessage(final Message message) {
        return Assert.notNull(message, () -> new ValidateException("Message must not be null"));
    }

    /**
     * Executable pipeline stage.
     *
     * @param checkpoint checkpoint boundary
     * @param processor  processor
     */
    public record Stage(Checkpoint checkpoint, Processor processor) {

        /**
         * Creates a validated executable stage.
         *
         * @param checkpoint checkpoint boundary
         * @param processor  processor
         */
        public Stage {
            checkpoint = validateCheckpoint(checkpoint);
            processor = validateProcessor(processor);
        }

    }

    /**
     * Pipeline stage processor.
     */
    @FunctionalInterface
    public interface Processor {

        /**
         * Processes a message at the current checkpoint.
         *
         * @param message current message
         * @param chain   downstream chain
         * @return processed message
         */
        Message process(Message message, Chain chain);

    }

    /**
     * Downstream pipeline chain.
     */
    public interface Chain {

        /**
         * Returns the current checkpoint boundary.
         *
         * @return checkpoint
         */
        Checkpoint checkpoint();

        /**
         * Proceeds to the next executable stage.
         *
         * @param message message
         * @return processed message
         */
        Message proceed(Message message);

    }

    /**
     * Runtime chain segment.
     */
    private final class ExecutionChain implements Chain {

        /**
         * Current checkpoint.
         */
        private final Checkpoint checkpoint;

        /**
         * Next stage index.
         */
        private final int nextIndex;

        /**
         * Whether the current checkpoint is terminal.
         */
        private final boolean terminal;

        /**
         * Proceed guard.
         */
        private boolean proceeded;

        /**
         * Creates a runtime chain segment.
         *
         * @param checkpoint checkpoint
         * @param nextIndex  next index
         * @param terminal   terminal boundary flag
         */
        private ExecutionChain(final Checkpoint checkpoint, final int nextIndex, final boolean terminal) {
            this.checkpoint = checkpoint;
            this.nextIndex = nextIndex;
            this.terminal = terminal;
        }

        @Override
        public Checkpoint checkpoint() {
            return checkpoint;
        }

        @Override
        public Message proceed(final Message message) {
            Assert.isFalse(proceeded, () -> new ProtocolException("Pipeline chain may proceed only once"));
            proceeded = true;
            final Message current = validateMessage(message);
            if (terminal) {
                return current;
            }
            return executeAt(nextIndex, current);
        }

    }

}
