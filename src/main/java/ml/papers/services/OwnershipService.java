package ml.papers.services;

import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import ml.papers.entities.tables.daos.OwnershipDao;
import ml.papers.entities.tables.pojos.Ownership;
import ml.papers.entities.tables.pojos.Paper;
import ml.papers.entities.tables.records.OwnershipRecord;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class OwnershipService extends BaseDbService {
    private final OwnershipDao ownershipDao;
    private final PaperService paperService;
    private final AuthorService authorService;
    private final LoggerService log;

    public OwnershipService(@Property(name = "db.user-name") String userName,
                            @Property(name = "db.password") String password,
                            @Property(name = "db.url") String url,
                            PaperService paperService,
                            AuthorService authorService,
                            LoggerService log) {
        super(userName, password, url);
        this.paperService = paperService;
        this.authorService = authorService;
        this.log = log;
        DSLContext context = DSL.using(connection, SQLDialect.MYSQL);
        ownershipDao = new OwnershipDao(context.configuration());
    }

    public void insertPapersOwnership(Collection<Integer> paperIds) {
        paperService.getPapers(paperIds).forEach(this::insertPaperOwnership);
    }

    public void insertPaperOwnership(Paper paper) {
        List<Integer> authorIds = new ArrayList<>();
        Collection<String> authors = paperService.fetchAuthorsFromString(paper.getAuthors());
        for (String author : authors) {
            Integer id = authorService.getAuthorId(author);
            if (id == null) {
                log.log(String.format("paperId: %s - can't find author id for %s", paper.getId(), author));
                System.out.println(id);
            } else {
                authorIds.add(id);
            }
        }
        ownershipDao.ctx().batchInsert(authorIds.stream().map(id -> {
            OwnershipRecord ownershipRecord = new OwnershipRecord();
            ownershipRecord.setAuthorId(id);
            ownershipRecord.setPaperId(paper.getId());
            return ownershipRecord;
        }).collect(Collectors.toSet())).execute();
    }

    public void insert(Paper paper, Integer authorId) {
        Ownership ownership = new Ownership();
        ownership.setAuthorId(authorId);
        ownership.setPaperId(paper.getId());
        ownershipDao.insert(ownership);
    }
}
