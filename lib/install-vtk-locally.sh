# The below command will create a local maven repository containing
# the VTK jar. 

mvn install:install-file -Dfile=vtk-9.1.0.jar \
                         -DgeneratePom=true \
                         -DgroupId=vtk \
                         -DartifactId=vtk \
                         -Dversion=9.1.0 \
                         -Dpackaging=jar \
                         -DcreateChecksum=true \
                         -DlocalRepositoryPath=../mvn
                         