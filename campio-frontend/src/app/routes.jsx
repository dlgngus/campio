import { Navigate } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout.jsx";
import LoginPage from "../pages/LoginPage.jsx";
import SignupPage from "../pages/SignupPage.jsx";
import OnboardingPage from "../pages/OnboardingPage.jsx";
import HomePage from "../pages/HomePage.jsx";
import ExplorePage from "../pages/ExplorePage.jsx";
import OpportunityDetailPage from "../pages/OpportunityDetailPage.jsx";
import SavedPage from "../pages/SavedPage.jsx";
import CommunityPage from "../pages/CommunityPage.jsx";
import MentorsPage from "../pages/MentorsPage.jsx";
import ProfilePage from "../pages/ProfilePage.jsx";
import AdminOpportunitiesPage from "../pages/AdminOpportunitiesPage.jsx";
import AdminIngestionPage from "../pages/AdminIngestionPage.jsx";
import CommunityDetailPage from "../pages/CommunityDetailPage.jsx";
import MentorDetailPage from "../pages/MentorDetailPage.jsx";
import AdminMentorsPage from "../pages/AdminMentorsPage.jsx";

const routes = [
  { path: "/", element: <Navigate to="/explore" replace /> },
  { path: "/login", element: <LoginPage /> },
  { path: "/signup", element: <SignupPage /> },
  { path: "/onboarding", element: <OnboardingPage /> },
  {
    element: <AppLayout />,
    children: [
      { path: "/home", element: <HomePage /> },
      { path: "/explore", element: <ExplorePage /> },
      { path: "/opportunities/:id", element: <OpportunityDetailPage /> },
      { path: "/saved", element: <SavedPage /> },
      { path: "/community", element: <CommunityPage /> },
      { path: "/community/:id", element: <CommunityDetailPage /> },
      { path: "/mentors", element: <MentorsPage /> },
      { path: "/mentors/:id", element: <MentorDetailPage /> },
      { path: "/profile", element: <ProfilePage /> },
      { path: "/admin/opportunities", element: <AdminOpportunitiesPage /> },
      { path: "/admin/ingestion", element: <AdminIngestionPage /> },
      { path: "/admin/mentors", element: <AdminMentorsPage /> },
    ],
  },
  { path: "*", element: <Navigate to="/home" replace /> },
];

export default routes;
