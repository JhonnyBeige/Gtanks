/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.main;

import gtanks.logger.Logger;
import gtanks.logger.Type;
import java.util.Arrays;

public class ServerException
extends Exception {
    private static final long serialVersionUID = 1L;

    public ServerException(String error) {
        super(error);
        Logger.log(Type.ERROR, "Throw server exception with message: " + error);
    }

    @Override
    public String toString() {
        return Arrays.toString(super.getStackTrace());
    }
}

