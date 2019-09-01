package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.service.exception.RatingException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Transactional
public class RatingServiceJPA implements RatingService {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void setRating(Rating rating) throws RatingException {
        Object ratingFromTable;
        try {
            ratingFromTable = entityManager.createNamedQuery("Rating.setRating")
                    .setParameter("game", rating.getGame())
                    .setParameter("player", rating.getPlayer())
                    .getSingleResult();
            entityManager.remove(ratingFromTable);
            entityManager.persist(rating);
        }catch (NoResultException e){
            entityManager.persist(rating);
        }
    }

    @Override
    public int getAverageRating(String game) throws RatingException {
        Double result = 0.0;

        try {
            Object tmp = this.entityManager.createNamedQuery("Rating.getAverage")
                    .setParameter("game", game).getSingleResult();
            if (tmp instanceof Double) {
                result = (double) tmp;
                return result.intValue();
            } else {
            }
        } catch (NoResultException e) {
            System.out.println(e.getMessage());
        }
        return result.intValue();
    }

    @Override
    public int getRating(String game, String player) throws RatingException {
        try {
            return (int) this.entityManager.createNamedQuery("Rating.getRating")
                    .setParameter("game", game)
                    .setParameter("player", player)
                    .getSingleResult();
        }catch (NoResultException e){
            System.out.println("This player isn't in the system!");
            return 0;
        }
    }
}