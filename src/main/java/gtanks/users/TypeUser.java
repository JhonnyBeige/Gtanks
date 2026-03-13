/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package gtanks.users;

public enum TypeUser {
    DEFAULT{

        public String toString() {
            return "default";
        }
    }
    ,
    MODERATOR{

        public String toString() {
            return "moderator";
        }
    }
    ,
    ADMIN{

        public String toString() {
            return "admin";
        }
    }
    ,
    TESTER{

        public String toString() {
            return "tester";
        }
    }
    ,
    SPECTATOR{

        public String toString() {
            return "spectator";
        }
    };

}

