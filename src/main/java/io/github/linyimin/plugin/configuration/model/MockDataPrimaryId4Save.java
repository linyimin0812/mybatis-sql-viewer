package io.github.linyimin.plugin.configuration.model;

import java.util.List;

/**
 * @author banzhe
 * @date 2022/12/04 15:56
 **/
public class MockDataPrimaryId4Save {

    private List<PrimaryIdInTable> list;

    public List<PrimaryIdInTable> getList() {
        return list;
    }

    public void setList(List<PrimaryIdInTable> list) {
        this.list = list;
    }

    public static class PrimaryIdInTable {

        private String table;
        private long minId;
        private long maxId;

        public PrimaryIdInTable() {}

        public PrimaryIdInTable(String table, long minId, long maxId) {
            this.table = table;
            this.minId = minId;
            this.maxId = maxId;
        }

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public long getMinId() {
            return minId;
        }

        public void setMinId(long minId) {
            this.minId = minId;
        }

        public long getMaxId() {
            return maxId;
        }

        public void setMaxId(long maxId) {
            this.maxId = maxId;
        }
    }
}
