package com.burstlinker.ezcapsolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.burstlinker.ezcapsolver.model.Solved;
import com.burstlinker.ezcapsolver.model.solution.BaseSolution;
import com.burstlinker.ezcapsolver.model.task.BaseTaskParams;
import com.burstlinker.ezcapsolver.model.task.TaskParams;
import com.burstlinker.ezcapsolver.model.task.TaskType;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Holds the rule that adding a task type means touching every place it belongs.
 *
 * <p>Four things have to line up for one task type: the {@link TaskType} constant, the
 * parameter model, the solution model, and the two convenience methods. Nothing in the
 * compiler notices when one is missing — the SDK just quietly cannot solve that type — so this
 * checks the whole set by reflection instead.
 *
 * <p>Every assertion here is designed to fail on a <em>partial</em> addition, which is the only
 * kind that actually happens.
 */
class TaskTypeCoverageTest {

    /** The task catalog documents 23. A change here should be a deliberate one. */
    private static final int EXPECTED_TASK_TYPES = 23;

    @Test
    @DisplayName("the catalog has every type exactly once")
    void knownTypesAreCompleteAndUnique() {
        assertEquals(EXPECTED_TASK_TYPES, TaskType.KNOWN.size());
        assertEquals(
                EXPECTED_TASK_TYPES,
                new HashSet<String>(TaskType.KNOWN).size(),
                "a wire type name is listed twice");

        for (String taskType : TaskType.KNOWN) {
            assertTrue(TaskType.isKnown(taskType), taskType + " is listed but isKnown says otherwise");
        }
    }

    @Test
    @DisplayName("every task type has both convenience methods, named after the wire type")
    void everyTypeHasBothMethods() {
        List<String> missing = new ArrayList<String>();
        for (String taskType : TaskType.KNOWN) {
            if (findMethod("solve" + taskType) == null) {
                missing.add("solve" + taskType);
            }
            if (findMethod("syncSolve" + taskType) == null) {
                missing.add("syncSolve" + taskType);
            }
        }
        // The name is mechanical: "solve" or "syncSolve" plus the wire type name, verbatim,
        // including the catalog's own irregular casing. No exceptions to look up.
        assertTrue(missing.isEmpty(), "missing convenience methods: " + missing);
    }

    @Test
    @DisplayName("both methods of a type take the same parameter model and return the same type")
    void bothMethodsAgree() {
        for (String taskType : TaskType.KNOWN) {
            Method async = findMethod("solve" + taskType);
            Method sync = findMethod("syncSolve" + taskType);
            assertNotNull(async, taskType);
            assertNotNull(sync, taskType);

            assertEquals(
                    async.getParameterTypes()[0],
                    sync.getParameterTypes()[0],
                    taskType + ": the two endpoints must take the same parameters");
            assertEquals(
                    async.getGenericReturnType().toString(),
                    sync.getGenericReturnType().toString(),
                    taskType + ": the two endpoints must produce the same solution");
        }
    }

    @Test
    @DisplayName("a model's declared solution type matches what its method returns")
    void solutionTypeMatchesTheMethodSignature() throws Exception {
        for (String taskType : TaskType.KNOWN) {
            Method method = findMethod("solve" + taskType);
            Class<?> paramsClass = method.getParameterTypes()[0];

            assertTrue(
                    BaseTaskParams.class.isAssignableFrom(paramsClass),
                    taskType + ": parameters must extend BaseTaskParams");

            // solutionType() is what the decoder actually uses at runtime; the generic return
            // type is what the caller sees at compile time. If they drift, the method hands
            // back a Solved<X> holding a Y and the ClassCastException lands far from here.
            TaskParams<?> instance = (TaskParams<?>) paramsClass.getDeclaredConstructor().newInstance();
            Class<?> declared = instance.solutionType();
            Class<?> fromSignature = solvedTypeArgumentOf(method);

            assertEquals(
                    fromSignature,
                    declared,
                    taskType + ": solutionType() disagrees with the method's return type");
        }
    }

    @Test
    @DisplayName("every parameter model on the classpath is reachable from a method")
    void noOrphanedParameterModels() {
        Set<String> used = new TreeSet<String>();
        for (String taskType : TaskType.KNOWN) {
            used.add(findMethod("solve" + taskType).getParameterTypes()[0].getName());
        }

        Set<String> orphans = new TreeSet<String>();
        for (Class<?> candidate : classesIn(BaseTaskParams.class)) {
            if (BaseTaskParams.class.isAssignableFrom(candidate)
                    && !Modifier.isAbstract(candidate.getModifiers())
                    && !used.contains(candidate.getName())) {
                orphans.add(candidate.getSimpleName());
            }
        }
        // Catches the half-finished addition: a model was written, the convenience methods
        // were not, and nothing else would ever say so.
        assertTrue(orphans.isEmpty(), "parameter models no method can reach: " + orphans);
    }

    @Test
    @DisplayName("every solution model on the classpath is reachable from a method")
    void noOrphanedSolutionModels() {
        Set<String> used = new TreeSet<String>();
        for (String taskType : TaskType.KNOWN) {
            used.add(solvedTypeArgumentOf(findMethod("solve" + taskType)).getName());
        }

        Set<String> orphans = new TreeSet<String>();
        for (Class<?> candidate : classesIn(BaseSolution.class)) {
            if (BaseSolution.class.isAssignableFrom(candidate)
                    && !Modifier.isAbstract(candidate.getModifiers())
                    && !used.contains(candidate.getName())) {
                orphans.add(candidate.getSimpleName());
            }
        }
        assertTrue(orphans.isEmpty(), "solution models no method can reach: " + orphans);
    }

    @Test
    @DisplayName("the documented synchronous types are the ones isSync reports")
    void syncTypesAreTheDocumentedSubset() {
        Set<String> sync = new TreeSet<String>();
        for (String taskType : TaskType.KNOWN) {
            if (TaskType.isSync(taskType)) {
                sync.add(taskType);
            }
        }

        // Written out rather than derived, so a type silently moving between the two lists is
        // visible in the diff. The service may reject a type on the endpoint it does not
        // serve, which costs a round trip -- not a task, since it is refused before billing.
        Set<String> expected = new TreeSet<String>();
        expected.add(TaskType.RECAPTCHA_V2_CLASSIFICATION);
        expected.add(TaskType.FUNCAPTCHA_CLASSIFICATION);
        expected.add(TaskType.HCAPTCHA_CLASSIFICATION);
        expected.add(TaskType.AKAMAI_WEB_TASK_PROXYLESS);
        expected.add(TaskType.AKAMAI_SBSD_TASK_PROXYLESS);
        expected.add(TaskType.TLS_TASK);
        expected.add(TaskType.DATADOME_TASK_PROXYLESS);
        expected.add(TaskType.DATADOME_TAGS_TASK_PROXYLESS);
        expected.add(TaskType.INCAPSULA_TASK_PROXYLESS);

        assertEquals(expected, sync);
        assertFalse(TaskType.isSync("NotATaskTypeAtAll"));
    }

    @Test
    @DisplayName("an unmodelled type is still usable through the escape hatch")
    void unknownTypesStayOpen() {
        // TaskType is constants over open strings, not an enum. A type the service adds after
        // this release has to work without waiting for an SDK update.
        assertFalse(TaskType.isKnown("BrandNewTaskType"));
        assertFalse(TaskType.isKnown(null));
    }

    private static Method findMethod(String name) {
        for (Method method : EzCapSolverClient.class.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 1) {
                return method;
            }
        }
        return null;
    }

    /** Pulls {@code X} out of a {@code Solved<X>} return type. */
    private static Class<?> solvedTypeArgumentOf(Method method) {
        Type returned = method.getGenericReturnType();
        if (!(returned instanceof ParameterizedType)) {
            return fail(method.getName() + " must return a parameterised Solved<...>");
        }
        ParameterizedType parameterized = (ParameterizedType) returned;
        assertEquals(Solved.class, parameterized.getRawType(), method.getName());
        Type argument = parameterized.getActualTypeArguments()[0];
        if (!(argument instanceof Class)) {
            return fail(method.getName() + " must name a concrete solution class");
        }
        return (Class<?>) argument;
    }

    /**
     * Every class compiled into a package, read off the build output.
     *
     * <p>Reflection alone cannot enumerate a package, and a class only shows up here once it
     * has been compiled — which is exactly the property that makes the orphan checks work
     * without a classpath-scanning dependency.
     */
    private static List<Class<?>> classesIn(Class<?> packageMarker) {
        // Derived from a class in the package rather than a literal, so moving the package
        // cannot quietly turn this check into a no-op.
        String packageName = packageMarker.getPackage().getName();
        URL location =
                TaskTypeCoverageTest.class.getResource("/" + packageName.replace('.', '/'));
        assertNotNull(location, "package not on the classpath: " + packageName);
        assertEquals(
                "file",
                location.getProtocol(),
                "these checks read the build output directory; running from a jar needs a different walk");

        File directory = new File(location.getPath().replace("%20", " "));
        File[] files = directory.listFiles();
        assertNotNull(files, "cannot list " + directory);

        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (File file : files) {
            String name = file.getName();
            // Skip nested and synthetic classes: Lombok's generated builders live there, and
            // they are not models.
            if (!name.endsWith(".class") || name.contains("$") || name.equals("package-info.class")) {
                continue;
            }
            try {
                classes.add(Class.forName(packageName + "." + name.substring(0, name.length() - 6)));
            } catch (ClassNotFoundException e) {
                fail("compiled but not loadable: " + name);
            }
        }
        assertFalse(classes.isEmpty(), "found no classes in " + packageName);
        return classes;
    }
}
