/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h14199.command.ddl;

import org.h14199.api.ErrorCode;
import org.h14199.command.CommandInterface;
import org.h14199.engine.Right;
import org.h14199.engine.Session;
import org.h14199.message.DbException;
import org.h14199.schema.Sequence;
import org.h14199.table.Column;
import org.h14199.table.Table;

/**
 * This class represents the statement
 * TRUNCATE TABLE
 */
public class TruncateTable extends DefineCommand {

    private Table table;

    private boolean restart;

    public TruncateTable(Session session) {
        super(session);
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setRestart(boolean restart) {
        this.restart = restart;
    }

    @Override
    public int update() {
        session.commit(true);
        if (!table.canTruncate()) {
            throw DbException.get(ErrorCode.CANNOT_TRUNCATE_1, table.getSQL(false));
        }
        session.getUser().checkRight(table, Right.DELETE);
        table.lock(session, true, true);
        table.truncate(session);
        if (restart) {
            for (Column column : table.getColumns()) {
                Sequence sequence = column.getSequence();
                if (sequence != null) {
                    long min = sequence.getMinValue();
                    if (min != sequence.getCurrentValue()) {
                        sequence.modify(min, null, null, null);
                        session.getDatabase().updateMeta(session, sequence);
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public int getType() {
        return CommandInterface.TRUNCATE_TABLE;
    }

}
