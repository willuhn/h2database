/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h14199.command.ddl;

import org.h14199.api.ErrorCode;
import org.h14199.command.CommandInterface;
import org.h14199.engine.Database;
import org.h14199.engine.Session;
import org.h14199.message.DbException;
import org.h14199.schema.Schema;
import org.h14199.schema.Sequence;

/**
 * This class represents the statement
 * DROP SEQUENCE
 */
public class DropSequence extends SchemaCommand {

    private String sequenceName;
    private boolean ifExists;

    public DropSequence(Session session, Schema schema) {
        super(session, schema);
    }

    public void setIfExists(boolean b) {
        ifExists = b;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @Override
    public int update() {
        session.getUser().checkAdmin();
        session.commit(true);
        Database db = session.getDatabase();
        Sequence sequence = getSchema().findSequence(sequenceName);
        if (sequence == null) {
            if (!ifExists) {
                throw DbException.get(ErrorCode.SEQUENCE_NOT_FOUND_1, sequenceName);
            }
        } else {
            if (sequence.getBelongsToTable()) {
                throw DbException.get(ErrorCode.SEQUENCE_BELONGS_TO_A_TABLE_1, sequenceName);
            }
            db.removeSchemaObject(session, sequence);
        }
        return 0;
    }

    @Override
    public int getType() {
        return CommandInterface.DROP_SEQUENCE;
    }

}
