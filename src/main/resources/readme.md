Dans les mappers, j'ai l'impression qu'il faut faire gaffe à la duplication de mêmes requêtes. Yen a deux qui partent sur les labels dans restaurantType, et j'ai l'impression que c'est faux. À vérifier



public void setDependenciesEval(CompleteEvaluationMapper completeEvalMapper,
GradeMapper gradeMapper,
BasicEvaluationMapper basicEvalMapper) {
}

    public void setDependenciesCityType(CityMapper cityMapper, RestaurantTypeMapper typeMapper){
        this.cityMapper = cityMapper;
        this.typeMapper = typeMapper;
    } a quoi sert cette merde