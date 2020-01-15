[![Codacy Badge](https://api.codacy.com/project/badge/Grade/291282703a484065ab603723e2ed9aaa)](https://www.codacy.com/app/ONSDigital_FWMT/census-fwmt-acceptance-tests?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ONSdigital/census-fwmt-acceptance-tests&amp;utm_campaign=Badge_Grade)

Acceptace tests for the FWMT

#Running services through docker

to run FWMT services locally through docker run
```
docker-compose up
```

To run end to end integration tests connected to RM follow instructions in [census rm docker dev](https://github.com/ONSdigital/census-rm-docker-dev)
and then run 
```
docker-compose -f docker-compose-rm-integration.yml up
```