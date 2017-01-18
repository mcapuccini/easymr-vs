docker run \
  -v $PWD/data/:/data mcapuccini/oe-docking \
  fred -receptor data/hiv1_protease.oeb \
  -dbase data/molecules_1.sdf \
  -docked_molecule_file data/dock_1.sdf \
  -hitlist_size 3

docker run \
  -v $PWD/data/:/data mcapuccini/oe-docking \
  fred -receptor data/hiv1_protease.oeb \
  -dbase data/molecules_2.sdf \
  -docked_molecule_file data/dock_2.sdf \
  -hitlist_size 3

docker run -v $PWD/data/:/data mcapuccini/oe-docking \
  scorepose -receptor data/hiv1_protease.oeb \
  -dbase data/dock_*.sdf \
  -hitlist_size 3 \
  -out data/global_top3.sdf
