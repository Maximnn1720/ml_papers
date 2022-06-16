package ml.papers.services;

import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import ml.papers.entities.Tables;
import ml.papers.entities.tables.Author;
import ml.papers.entities.tables.daos.AuthorDao;
import ml.papers.entities.tables.records.AuthorRecord;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.max;

@Singleton
public class AuthorService extends BaseDbService {
    private final AuthorDao authorDao;
    private final LoggerService log;

    public AuthorService(@Property(name = "db.user-name") String userName,
                         @Property(name = "db.password") String password,
                         @Property(name = "db.url") String url, LoggerService log) {
        super(userName, password, url);
        this.log = log;
        DSLContext context = DSL.using(connection, SQLDialect.MYSQL);
        authorDao = new AuthorDao(context.configuration());
    }

    public void insertAuthors(Collection<String> authorNames) {
        DSLContext context = authorDao.ctx();
        var authors = authorNames.stream().map(name -> {
            AuthorRecord authorRecord = context.newRecord(Author.AUTHOR);
            authorRecord.setAuthorName(name);
            return authorRecord;
        }).collect(Collectors.toList());
        context.batchInsert(authors).execute();
    }

    public Integer getAuthorId(String author) {
        ml.papers.entities.tables.pojos.Author authorEntity = authorDao.fetchOneByAuthorName(author);
        return authorEntity == null ? null : authorEntity.getId();
    }

    public String getMostHardworkingAuthor() {
        DSLContext context = authorDao.ctx();
        var sub_query_own = context.select(Tables.OWNERSHIP.AUTHOR_ID, count().as("papers_count")).from(Tables.OWNERSHIP).groupBy(Tables.OWNERSHIP.AUTHOR_ID);
        var sub_query_max_count = context.select(max(sub_query_own.field("papers_count")).as("max_count")).from(sub_query_own);
        var sub_query_author_id = context.select().from(sub_query_own)
                .where(sub_query_own.field("papers_count", Integer.class)
                        .eq(sub_query_max_count.fetch().getValues("max_count", Integer.class).stream().findFirst().get()));
        var query = context.select(sub_query_author_id.field("author_id"), sub_query_author_id.field("papers_count"), Author.AUTHOR.AUTHOR_NAME)
                .from(sub_query_author_id)
                .join(Author.AUTHOR).on(sub_query_author_id.field("author_id", Author.AUTHOR.ID.getDataType()).eq(Author.AUTHOR.ID));
        log.log(query.getSQL());
        return query.fetch().get(0).formatJSON();
    }
}

