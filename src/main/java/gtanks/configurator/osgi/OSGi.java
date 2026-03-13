/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.configurator.osgi;

import java.util.HashMap;

public class OSGi {
    private static HashMap<Class<?>, Object> models = new HashMap();

    public static void registerModel(Object model) {
        models.put(model.getClass(), model);
    }

    public static Object getModelByInterface(Class<?> _interface) {
        return models.get(_interface);
    }
}

