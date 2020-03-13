package edu.rit.nerParser.batch;

import edu.rit.nerParser.NistData;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Log4j2
public class NistDataTask  implements Tasklet {
  private final NistData nistData;

  public NistDataTask(NistData nistData) {
    super();
    this.nistData = nistData;
  }

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
    log.info("Entering NistDataTask.execute");
    nistData.load();
    return RepeatStatus.FINISHED;
  }
}
