package ml.papers.services;

import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import ml.papers.entities.Tables;
import ml.papers.entities.tables.daos.PaperDao;
import ml.papers.entities.tables.pojos.Paper;
import org.apache.commons.lang.StringUtils;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


@Singleton
public class PaperService extends BaseDbService {

    private final PaperDao paperDao;

    public PaperService(@Property(name = "db.user-name") String userName,
                        @Property(name = "db.password") String password,
                        @Property(name = "db.url") String url) {
        super(userName, password, url);
        DSLContext context = DSL.using(connection, SQLDialect.MYSQL);
        paperDao = new PaperDao(context.configuration());
    }

    public Paper getRandomPaper() {
        return paperDao.fetchOneById(((int) (Math.random() * paperDao.count())));
    }

    public Set<String> getAuthors() {
        return paperDao.findAll()
                .stream()
                .map(Paper::getAuthors)
                .flatMap(row -> fetchAuthorsFromString(row).stream())
                .filter(row -> row.chars().count() > 4)
                .collect(Collectors.toSet());
    }

    public Collection<String> fetchAuthorsFromString(String authorsString){
        authorsString = StringUtils.strip(authorsString, "[");
        authorsString = StringUtils.strip(authorsString, "]");
        return Arrays.stream(authorsString.split(","))
                .map(String::trim)
                .map(str -> StringUtils.strip(str, "'"))
                .collect(Collectors.toList());
    }

    public Collection<Paper> getPapers(Collection<Integer> paperIds) {
        return paperDao.fetch(Tables.PAPER.ID, paperIds);
    }

    public Collection<Integer> getPapersIds() {
        return paperDao.findAll().stream().map(Paper::getId).collect(Collectors.toList());
    }
}
