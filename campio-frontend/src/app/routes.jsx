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

const routes = [
  { path: "/", element: <Navigate to="/home" replace /> },
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
      { path: "/mentors", element: <MentorsPage /> },
      { path: "/profile", element: <ProfilePage /> },
      { path: "/admin/opportunities", element: <AdminOpportunitiesPage /> },
    ],
  },
  { path: "*", element: <Navigate to="/home" replace /> },
];

export default routes;
